package com.zeki.webclient

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "keys")
class ApiStatics {
    lateinit var webhook: Webhook
    lateinit var kis: Kis
    lateinit var dataGo: DataGo

    class Webhook {
        lateinit var url: String
        lateinit var key: String
        lateinit var token: String
    }

    class Kis {
        lateinit var url: String
        lateinit var appKey: String
        lateinit var appSecret: String
        lateinit var accountNumber: String
    }

    class DataGo {
        lateinit var url: String
        lateinit var encoding: String
        lateinit var decoding: String
    }
}