package com.zeki.kisserver


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class KisVolKotlinApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    @Suppress("SpreadOperator")
    runApplication<KisVolKotlinApplication>(*args)
}
