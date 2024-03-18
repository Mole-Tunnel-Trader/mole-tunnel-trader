package com.zeki.kisvolkotlin


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class KisVolKotlinApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<KisVolKotlinApplication>(*args)
}
