package com.zeki.data_go


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.util.*

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["com.zeki"])
@ComponentScan(basePackages = ["com.zeki"])
@EntityScan("com.zeki")
class KisVolKotlinApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    @Suppress("SpreadOperator")
    runApplication<KisVolKotlinApplication>(*args)
}
