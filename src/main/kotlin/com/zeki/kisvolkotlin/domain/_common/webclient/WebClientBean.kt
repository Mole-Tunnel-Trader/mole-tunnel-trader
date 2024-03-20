package com.zeki.kisvolkotlin.domain._common.webclient

import com.zeki.kisvolkotlin.exception.ApiException
import com.zeki.kisvolkotlin.exception.ResponseCode
import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.tcp.SslProvider
import java.time.Duration
import javax.net.ssl.SSLException

@Configuration
class WebClientBean(
    val webClientBuilder: WebClient.Builder,
    val apiStatics: ApiStatics
) {
    /**
     * WebClient의 baseUrl과 defaultHeader, encoding 설정
     *
     * @return [WebClient]
     */
    @Bean
    @Qualifier("WebClient")
    fun setBaseUrl(): WebClient {
        val baseUrl = ""

        // 인코딩 설정
        val factory = DefaultUriBuilderFactory(baseUrl)
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY

        // memory size 설정
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer: ClientCodecConfigurer ->
                configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)
            } // to unlimited memory size
            .build()

        // timeout 설정
        val httpConnector = ReactorClientHttpConnector(
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120000)
                .responseTimeout(Duration.ofSeconds(120))
        )


        return webClientBuilder
            .exchangeStrategies(exchangeStrategies)
            .clientConnector(httpConnector)
            .uriBuilderFactory(factory)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .baseUrl(baseUrl)
            .build()
    }


    /**
     * WebClient TLS 1.3 적용 버전
     *
     * @return [WebClient]
     */
    @Bean
    @Qualifier("WebClientKIS")
    fun setBaseUrlKis(): WebClient {
        val baseUrl: String = apiStatics.kis.url

        // 인코딩 설정
        val factory = DefaultUriBuilderFactory(baseUrl)
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY

        // memory size 설정
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer: ClientCodecConfigurer ->
                configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)
            } // to unlimited memory size
            .build()


        // The connection observed an error  reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response
        // 위 에러 떄문에 추가 함.(패킷 단계에서 분석해야 함) 'io.micrometer:micrometer-core' 를 dependency 에 추가해야 함.
        val provider = ConnectionProvider.builder("custom-provider")
            .maxConnections(100)
            .maxIdleTime(Duration.ofSeconds(3))
            .maxLifeTime(Duration.ofSeconds(3))
            .pendingAcquireTimeout(Duration.ofMillis(5000))
            .pendingAcquireMaxCount(-1)
            .evictInBackground(Duration.ofSeconds(30))
            .lifo()
            .metrics(true)
            .build()

        val sslContextForTls13: SslContext
        try {
            sslContextForTls13 = SslContextBuilder.forClient()
                .protocols("TLSv1.3")
                .build()
        } catch (e: SSLException) {
            throw ApiException(ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR, e.message ?: "SSL 설정 오류")
        }

        val httpClientForTls13 = HttpClient.create(provider)
            .secure { ssl: SslProvider.SslContextSpec -> ssl.sslContext(sslContextForTls13) }
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120000) // timeout 설정
            .responseTimeout(Duration.ofSeconds(120))

        val reactorClientHttpConnector = ReactorClientHttpConnector(httpClientForTls13)

        return webClientBuilder
            .exchangeStrategies(exchangeStrategies)
            .clientConnector(reactorClientHttpConnector)
            .uriBuilderFactory(factory)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .baseUrl(baseUrl)
            .build()
    }

    /**
     * WebClient TLS 1.3 적용 버전
     *
     * @return [WebClient]
     */
    @Bean
    @Qualifier("WebClientDataGo")
    fun setBaseUrlDataGo(): WebClient {
        val url: String = apiStatics.dataGo.url + "?serviceKey=" + apiStatics.dataGo.encoding

        // 인코딩 설정
        val factory = DefaultUriBuilderFactory(url)
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY

        // memory size 설정
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer: ClientCodecConfigurer ->
                configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)
            } // to unlimited memory size
            .build()


        val sslContext: SslContext
        try {
            sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build()
        } catch (e: SSLException) {
            throw ApiException(ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR, "SSL 설정 오류")
        }

        val reactorClientHttpConnector = ReactorClientHttpConnector(
            HttpClient.create()
                .secure { t: SslProvider.SslContextSpec -> t.sslContext(sslContext) }
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120000) // timeout 설정
                .responseTimeout(Duration.ofSeconds(120)))

        return webClientBuilder
            .exchangeStrategies(exchangeStrategies)
            .clientConnector(reactorClientHttpConnector)
            .uriBuilderFactory(factory)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .baseUrl(url)
            .build()
    }
}