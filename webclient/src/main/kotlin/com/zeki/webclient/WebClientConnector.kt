package com.zeki.webclient

import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.common.util.CustomUtils
import io.netty.handler.codec.http.HttpHeaders.addHeader
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.springframework.core.env.Environment
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
    private val env: Environment,
    private val client: OkHttpClient,  // 빈으로 주입된 OkHttpClient 사용
    private val objectMapper: ObjectMapper // JSON 직렬화를 위한 ObjectMapper
) {
    enum class WebClientType {
        KIS, DATA_GO, DEFAULT
    }

    // 최근 1초 내의 요청 타임스탬프를 저장하는 큐
    private val requestTimestamps: ConcurrentLinkedQueue<Long> = ConcurrentLinkedQueue()

    // 동시성 제어를 위한 락
    private val lock = ReentrantLock()

    /**
     * WebClient를 이용하여 API 호출을 수행합니다.
     *
     * @param webClientType 사용하려는 WebClient의 타입 (KIS, DATA_GO, DEFAULT)
     * @param method HTTP 메서드 (GET, POST 등)
     * @param path API 엔드포인트 경로
     * @param requestHeaders 요청 헤더
     * @param requestParams 요청 쿼리 파라미터
     * @param requestBody 요청 본문
     * @param responseClassType 응답을 매핑할 클래스 타입
     * @param retryCount 재시도 횟수
     * @param retryDelay 재시도 간 대기 시간 (밀리초)
     * @return 응답 객체 또는 null
     */
    fun <Q, S> connect(
        webClientType: WebClientType,
        method: String,
        path: String,
        requestHeaders: Map<String, String> = mapOf(),
        requestParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        requestBody: Q? = null,
        responseClassType: Class<S>,
        retryCount: Int = 0,
        retryDelayMillis: Long = 0,
        isRealTrade: Boolean = false
    ): S? {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestBody?.let { RequestBody.create(mediaType, it.toString()) }


        val keyKisAccountNum = env.getProperty("keys.kis.account-number")
            ?: throw ApiException(ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR)
        val requestBuilder = when (webClientType) {
            WebClientType.KIS -> {
                Request.Builder()
                    .url(
                        env.getProperty("keys.kis.url")
                            ?: throw ApiException(ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR)
                    )
                    .method(method, body)
                    .addHeader(
                        "appkey",
                        env.getProperty("keys.kis.app-key")
                            ?: throw ApiException(ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR)
                    )
                    .addHeader(
                        "appsecret",
                        env.getProperty("keys.kis.app-secret")
                            ?: throw ApiException(ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR)
                    )
            }

            WebClientType.DATA_GO -> "http://localhost:8081$path"
            WebClientType.DEFAULT -> "http://localhost:8082$path"
        }

        requestBuilder.apply {
            requestHeaders.forEach { (key, value) ->
                addHeader(key, value)
            }
        }


        val request = requestBuilder.build()

        repeat(retryCount + 1) { attempt ->
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return response.body?.string()?.let {
                            objectMapper.readValue(it, responseClassType)
                        }
                    }
                }
            } catch (e: Exception) {
                println("Request failed on attempt ${attempt + 1}: ${e.message}")
                if (attempt < retryCount) {
                    Thread.sleep(retryDelayMillis)
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
                // 1초 이전의 타임스탬프 제거
                while (requestTimestamps.isNotEmpty() && currentTime - requestTimestamps.peek() > 1000) {
                    requestTimestamps.poll()
                }

                val cnt: Int = if (CustomUtils.isProdProfile(env)) 19 else 2

                if (requestTimestamps.size < cnt) {
                    // 퍼밋 허용
                    requestTimestamps.add(currentTime)
                    return
                }

                // 제한을 초과한 경우 대기 시간 계산
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
