package com.zeki.ok_http_client

import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ExceptionUtils
import com.zeki.common.exception.ExceptionUtils.log
import com.zeki.common.exception.ResponseCode
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
import java.io.IOException
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class OkHttpClientConnector(
    private val apiStatics: ApiStatics,
    private val client: OkHttpClient,  // OkHttpClient 사용
    private val objectMapper: ObjectMapper,
    private val env: Environment
) {
    enum class ClientType {
        KIS, DATA_GO, NAVER_FINANCE, DEFAULT
    }

    data class ApiResponse<S>(
        val isSuccess: Boolean,
        val body: S?,
        val headers: Map<String, List<String>>?
    )

    private val requestTimestamps: ConcurrentLinkedQueue<Long> = ConcurrentLinkedQueue()
    private val lock = ReentrantLock()

    /**
     * OkHttpClient를 이용한 API 호출
     */
    fun <Q, S : Any> connect(
        clientType: ClientType,
        method: HttpMethod,
        path: String,
        requestHeaders: Map<String, String> = mapOf(),
        requestParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        requestBody: Q? = null,
        responseClassType: Class<S>,
        retryCount: Int = 0,
        retryDelay: Long = 0
    ): ApiResponse<S> {
        val mediaType = "application/json; charset=utf-8".toMediaType()

        // 요청 파라미터를 URL 쿼리 스트링으로 변환
        val queryString = requestParams.entries.joinToString("&") { (key, values) ->
            values.joinToString("&") { "$key=${it}" }
        }.takeIf { it.isNotEmpty() }?.let { "?$it" } ?: ""

        // 요청 URL 구성
        val baseUrl = when (clientType) {
            ClientType.KIS -> apiStatics.kis.url
            ClientType.DATA_GO -> apiStatics.dataGo.url
            ClientType.DEFAULT -> ""
            ClientType.NAVER_FINANCE -> ""
        }
        var url = if (baseUrl.isBlank()) {
            "$path$queryString"
        } else {
            "$baseUrl/$path$queryString"
        }

        if (clientType == ClientType.DATA_GO) {
            url = url + "&serviceKey=" + apiStatics.dataGo.encoding
        }

        // GET, DELETE 요청 시 body는 null, 그 외에는 JSON 변환 후 RequestBody 생성
        val body: RequestBody? = when (method) {
            HttpMethod.GET, HttpMethod.DELETE -> null
            else -> requestBody?.let {
                objectMapper.writeValueAsString(it).toRequestBody(mediaType)
            }
        }

        // Request.Builder 생성
        val requestBuilder = Request.Builder().url(url).method(method.name(), body)

        // WebClientType에 따라 추가 헤더 설정
        when (clientType) {
            ClientType.KIS -> {
//                requestBuilder.addHeader("appkey", apiStatics.kis.appKey)
//                requestBuilder.addHeader("appsecret", apiStatics.kis.appSecret)
            }

            ClientType.DATA_GO -> {
                requestBuilder.addHeader("Content-Type", "application/json")
            }

            ClientType.DEFAULT -> {}

            ClientType.NAVER_FINANCE -> {}
        }

        // 요청 헤더 추가
        requestHeaders.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()

        enforceRateLimit()

        client.newCall(request).execute().use { response ->
            val headers = response.headers.toMultimap()

            val responseBodyString = response.body?.string()?.trim()
            if (responseClassType == Unit::class.java ||
                responseClassType == Void::class.java ||
                responseBodyString.isNullOrEmpty()
            ) {
                return ApiResponse(response.isSuccessful, null, headers)
            }

            val contentType = response.header("Content-Type")?.lowercase() ?: ""

            val responseBody: Any? = try {
                // JSON 응답인지 확인 후 파싱
                if (clientType != ClientType.NAVER_FINANCE) {
                    objectMapper.readValue(responseBodyString, responseClassType)
                } else {
                    responseBodyString // JSON이 아니면 문자열 그대로 반환
                }
            } catch (e: IOException) {
                log.error("JSON 파싱 오류: ${e.message}, 응답 내용: $responseBodyString")
                throw ApiException(
                        ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR
                )
            }

            @Suppress("UNCHECKED_CAST")
            return ApiResponse(response.isSuccessful, responseBody as S, headers)
        }
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
                        ExceptionUtils.logError(e)
                        return
                    }
                }
            }
        }
    }
}
