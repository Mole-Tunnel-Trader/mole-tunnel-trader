package com.zeki.webclient

import com.zeki.common.util.CustomUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class WebClientConnector(
    @Qualifier("WebClientKIS") private val webClientKis: WebClient,
    @Qualifier("WebClientDataGo") private val webClientDataGo: WebClient,
    @Qualifier("WebClient") private val webClient: WebClient,
    private val env: Environment
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
        method: HttpMethod,
        path: String,
        requestHeaders: Map<String, String> = mapOf(),
        requestParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        requestBody: Q? = null,
        responseClassType: Class<S>,
        retryCount: Long = 0,
        retryDelay: Long = 0
    ): ResponseEntity<S>? {
        // 요청 제한 로직 적용
        this.enforceRateLimit()

        val selectedWebClient = when (webClientType) {
            WebClientType.KIS -> webClientKis
            WebClientType.DATA_GO -> webClientDataGo
            WebClientType.DEFAULT -> webClient
        }

        val retrySpec = Retry.fixedDelay(retryCount, java.time.Duration.ofMillis(retryDelay))

        val responseMono: Mono<ResponseEntity<S>> = selectedWebClient
            .method(method)
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(path).queryParams(requestParams).build()
            }
            .headers { httpHeaders: HttpHeaders -> httpHeaders.setAll(requestHeaders) }
            .bodyValue(requestBody ?: mapOf<String, String>())
            .exchangeToMono { clientResponse: ClientResponse ->
                clientResponse.toEntity(responseClassType)
            }
            .retryWhen(retrySpec)

        return try {
            responseMono.block()
        } catch (e: Exception) {
            println("Error during WebClient request: ${e.message}")
            null
        }
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
