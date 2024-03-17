package com.zeki.kisvolkotlin.config

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class SchemaInit : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
//        SchemaUtils.create(
//            tables = arrayOf(
//                HolidayEntity
//            ),
//            inBatch = true
//        )
    }
}