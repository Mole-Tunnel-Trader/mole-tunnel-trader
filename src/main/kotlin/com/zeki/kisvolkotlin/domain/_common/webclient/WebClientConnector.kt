package com.zeki.kisvolkotlin.domain._common.webclient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.util.retry.Retry
import java.time.Duration

@Component
class WebClientConnector(
    @Qualifier("WebClientKIS") private val webClientKis: WebClient,
    @Qualifier("WebClientDataGo") private val webClientDataGo: WebClient,
    @Qualifier("WebClient") private val webClient: WebClient,
) {
    enum class WebClientType {
        KIS, DATA_GO, DEFAULT
    }

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
        val selectedWebClient = when (webClientType) {
            WebClientType.KIS -> webClientKis
            WebClientType.DATA_GO -> webClientDataGo
            WebClientType.DEFAULT -> webClient
        }

        val retry = Retry.fixedDelay(retryCount, Duration.ofMillis(retryDelay))

        return selectedWebClient
            .method(method)
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(path).queryParams(requestParams).build()
            }
            .headers { httpHeaders: HttpHeaders -> httpHeaders.setAll(requestHeaders) }
            .bodyValue(requestBody ?: mapOf<String, String>())
            .exchangeToMono { clientResponse: ClientResponse ->
                clientResponse.toEntity(responseClassType)
            }
            .retryWhen(retry)
            .block()
    }

}