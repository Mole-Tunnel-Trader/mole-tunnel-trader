package com.zeki.kisvolkotlin.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

object TestUtils {

    inline fun <reified T> loadJsonData(filePath: String): T {
        val mapper = ObjectMapper().registerKotlinModule()
        return mapper.readValue(File(filePath), T::class.java)
    }

    fun loadString(filePath: String): String = File(filePath).readText(Charsets.UTF_8)
}