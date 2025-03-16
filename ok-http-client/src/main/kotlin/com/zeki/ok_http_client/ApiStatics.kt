package com.zeki.ok_http_client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "keys")
class ApiStatics {
    lateinit var discord: Discord
    lateinit var dataGo: DataGo

    class DataGo {
        lateinit var url: String
        lateinit var encoding: String
        lateinit var decoding: String
    }

    class Discord {
        lateinit var reportUrl: String
    }

}