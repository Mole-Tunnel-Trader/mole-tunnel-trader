package com.zeki.ok_http_client

import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.common.em.TradeMode
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ExceptionUtils.log
import com.zeki.common.exception.ResponseCode
import com.zeki.ok_http_client.OkHttpClientConnector.ClientType.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.io.IOException

@Component
class OkHttpClientConnector(
    private val apiStatics: ApiStatics,
    private val client: OkHttpClient, // OkHttpClient 사용
    private val objectMapper: ObjectMapper,
    private val rateLimiter: RateLimiter
) {
    enum class ClientType {
        DATA_GO,
        DISCORD,
        DEFAULT
    }

    data class ApiResponse<S>(
        val isSuccess: Boolean,
        val body: S?,
        val headers: Map<String, List<String>>?
    )

    /** OkHttpClient를 이용한 API 호출 */
    fun <Q, S : Any> connect(
        clientType: ClientType,
        method: HttpMethod,
        path: String,
        requestHeaders: Map<String, String> = mapOf(),
        requestParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        requestBody: Q? = null,
        responseClassType: Class<S>
    ): ApiResponse<S> {
        val mediaType = "application/json; charset=utf-8".toMediaType()

        // 요청 파라미터를 URL 쿼리 스트링으로 변환
        val queryString =
            requestParams
                .entries
                .joinToString("&") { (key, values) ->
                    values.joinToString("&") { "$key=${it}" }
                }
                .takeIf { it.isNotEmpty() }
                ?.let { "?$it" }
                ?: ""

        // 요청 URL 구성
        val baseUrl =
            when (clientType) {
                DATA_GO -> apiStatics.dataGo.url
                DISCORD -> apiStatics.discord.reportUrl
                DEFAULT -> ""
            }
        var url =
            if (baseUrl.isBlank()) {
                "$path$queryString"
            } else {
                "$baseUrl/$path$queryString"
            }

        if (clientType == DATA_GO) {
            url = url + "&serviceKey=" + apiStatics.dataGo.encoding
        }

        // GET, DELETE 요청 시 body는 null, 그 외에는 JSON 변환 후 RequestBody 생성
        val body: RequestBody? =
            when (method) {
                HttpMethod.GET, HttpMethod.DELETE -> null
                else ->
                    requestBody?.let {
                        objectMapper.writeValueAsString(it).toRequestBody(mediaType)
                    }
            }

        // Request.Builder 생성
        val requestBuilder = Request.Builder().url(url).method(method.name(), body)

        // 요청 헤더 추가
        requestHeaders.forEach { (key, value) -> requestBuilder.addHeader(key, value) }

        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            val headers = response.headers.toMultimap()

            val responseBodyString = response.body?.string()
            if (responseClassType == Unit::class.java ||
                responseClassType == Void::class.java ||
                responseBodyString.isNullOrEmpty()
            ) {
                return ApiResponse(response.isSuccessful, null, headers)
            }

            val responseBody =
                try {
                    // JSON 파싱 시 예외 처리
                    responseBodyString.let { objectMapper.readValue(it, responseClassType) }
                } catch (e: IOException) {
                    log.error(e.message)
                    throw ApiException(ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR)
                }

            return ApiResponse(response.isSuccessful, responseBody, headers)
        }
    }

    /** OkHttpClient를 이용한 API 호출 */
    fun <Q, S : Any> connectKis(
        method: HttpMethod,
        path: String,
        requestHeaders: Map<String, String> = mapOf(),
        requestParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        requestBody: Q? = null,
        responseClassType: Class<S>,
        appkey: String,
        appsecret: String,
        accountType: TradeMode,
    ): ApiResponse<S> {
        rateLimiter.waitForRateLimit(appkey, accountType)

        val mediaType = "application/json; charset=utf-8".toMediaType()


        // 요청 파라미터를 URL 쿼리 스트링으로 변환
        val queryString =
            requestParams
                .entries
                .joinToString("&") { (key, values) ->
                    values.joinToString("&") { "$key=${it}" }
                }
                .takeIf { it.isNotEmpty() }
                ?.let { "?$it" }
                ?: ""

        // 요청 URL 구성
        val baseUrl =
            when (accountType) {
                TradeMode.REAL, TradeMode.BATCH -> "https://openapi.koreainvestment.com:9443"
                TradeMode.TRAIN -> "https://openapivts.koreainvestment.com:29443"
            }
        var url = "$baseUrl/$path$queryString"

        // GET, DELETE 요청 시 body는 null, 그 외에는 JSON 변환 후 RequestBody 생성
        val body: RequestBody? =
            when (method) {
                HttpMethod.GET, HttpMethod.DELETE -> null
                else ->
                    requestBody?.let {
                        objectMapper.writeValueAsString(it).toRequestBody(mediaType)
                    }
            }

        // Request.Builder 생성
        val requestBuilder = Request.Builder().url(url).method(method.name(), body)
        // WebClientType에 따라 추가 헤더 설정

        requestBuilder.addHeader("appkey", appkey)
        requestBuilder.addHeader("appsecret", appsecret)

        // 요청 헤더 추가
        requestHeaders.forEach { (key, value) -> requestBuilder.addHeader(key, value) }

        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            val headers = response.headers.toMultimap()

            val responseBodyString = response.body?.string()
            if (responseClassType == Unit::class.java ||
                responseClassType == Void::class.java ||
                responseBodyString.isNullOrEmpty()
            ) {
                return ApiResponse(response.isSuccessful, null, headers)
            }

            val responseBody =
                try {
                    // JSON 파싱 시 예외 처리
                    responseBodyString.let { objectMapper.readValue(it, responseClassType) }
                } catch (e: IOException) {
                    log.error(e.message)
                    throw ApiException(ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR)
                }

            return ApiResponse(response.isSuccessful, responseBody, headers)
        }
    }
}
