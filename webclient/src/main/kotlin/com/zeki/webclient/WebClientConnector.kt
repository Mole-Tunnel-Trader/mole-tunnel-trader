package com.zeki.webclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.common.util.CustomUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class WebClientConnector(
    private val apiStatics: ApiStatics,
    private val client: OkHttpClient,  // OkHttpClient 사용
    private val objectMapper: ObjectMapper,
    private val env: Environment
) {
    enum class WebClientType {
        KIS, DATA_GO, DEFAULT
    }

    data class ApiResponse<S>(
        val body: S?,
        val headers: Map<String, List<String>>?
    )

    private val requestTimestamps: ConcurrentLinkedQueue<Long> = ConcurrentLinkedQueue()
    private val lock = ReentrantLock()

    /**
     * OkHttpClient를 이용한 API 호출
     */
    fun <Q, S : Any> connect(
        webClientType: WebClientType,
        method: HttpMethod,
        path: String,
        requestHeaders: Map<String, String> = mapOf(),
        requestParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        requestBody: Q? = null,
        responseClassType: Class<S>,
        retryCount: Int = 0,
        retryDelay: Long = 0
    ): ApiResponse<S>? {
        val mediaType = "application/json; charset=utf-8".toMediaType()

        // GET, DELETE 요청 시 body는 null
        val body: RequestBody? = if (method.name() == "GET" || method.name() == "DELETE") {
            null
        } else {
            requestBody?.toString()?.toRequestBody(mediaType)
        }

        val requestBuilder = when (webClientType) {
            WebClientType.KIS -> {
                val url = apiStatics.kis.url + "/" + path
                val appKey = apiStatics.kis.appKey
                val appSecret = apiStatics.kis.appSecret

                Request.Builder()
                    .url(url)
                    .method(method.name(), body)
                    .addHeader("appkey", appKey)
                    .addHeader("appsecret", appSecret)
            }

            WebClientType.DATA_GO -> {
                val url = apiStatics.dataGo.url + "/" + path + "?serviceKey=" + apiStatics.dataGo.encoding
                Request.Builder().url(url)
            }

            WebClientType.DEFAULT -> {
                Request.Builder().url(path)
            }
        }

        requestBuilder.apply {
            requestHeaders.forEach { (key, value) ->
                addHeader(key, value)
            }
        }

        val request = requestBuilder.build()

        repeat(retryCount + 1) { attempt ->
            try {
                // OkHttpClient의 동기 요청 처리
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()?.let {
                            objectMapper.readValue(it, responseClassType)
                        }
                        val headers = response.headers.toMultimap()
                        return ApiResponse(body, headers)
                    } else {
                        println("Request failed with status: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                println("Request failed on attempt ${attempt + 1}: ${e.message}")
                if (attempt < retryCount) {
                    Thread.sleep(retryDelay)
                }
            }
        }

        return null
    }


    /**
     * 초당 20건의 요청 제한을 강제합니다.
     */
    private fun enforceRateLimit() {
        while (true) {
            val currentTime = Instant.now().toEpochMilli()

            lock.withLock {
                while (requestTimestamps.isNotEmpty() && currentTime - requestTimestamps.peek() > 1000) {
                    requestTimestamps.poll()
                }

                val cnt: Int = if (CustomUtils.isProdProfile(env)) 19 else 2

                if (requestTimestamps.size < cnt) {
                    requestTimestamps.add(currentTime)
                    return
                }

                val earliestTimestamp = requestTimestamps.peek() ?: currentTime
                val waitTime = 1100 - (currentTime - earliestTimestamp)

                if (waitTime > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(waitTime)
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        println("Interrupted while waiting for rate limit: ${e.message}")
                        return
                    }
                }
            }
        }
    }
}
