package com.zeki.kisvolkotlin

import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@ImportAutoConfiguration(ExposedAutoConfiguration::class)
class KisVolKotlinApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<KisVolKotlinApplication>(*args)
}
