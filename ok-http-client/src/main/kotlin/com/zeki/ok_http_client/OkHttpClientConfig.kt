package com.zeki.ok_http_client

import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class OkHttpClientConfig {

    @Bean
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)  // 연결 제한 시간
            .readTimeout(10, TimeUnit.SECONDS)     // 읽기 제한 시간
            .writeTimeout(10, TimeUnit.SECONDS)    // 쓰기 제한 시간
            .retryOnConnectionFailure(true)        // 연결 실패 시 재시도
            .build()
    }
}
