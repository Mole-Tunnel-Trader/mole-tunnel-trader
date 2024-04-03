package com.zeki.kisserver.domain._common.log

import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.kisserver.exception.ResponseCode
import com.zeki.kisvolkotlin.exception.ResponseCode
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

@Component
class GoogleChatConnector(
    private val objectMapper: ObjectMapper
) {

    fun connect(httpMethod: String, url: String, reqBody: Map<String, String>): String {
        var conn: HttpURLConnection? = null
        try {
            val body = objectMapper.writeValueAsString(reqBody)

            conn = URL(url).openConnection() as HttpURLConnection

            conn.requestMethod = httpMethod
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.doInput = true

            val outStream = OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8)
            val writer = PrintWriter(outStream)
            writer.write(body)
            writer.flush()

            val tmp = InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)
            val reader = BufferedReader(tmp)
            val builder = StringBuilder()
            var str: String?
            while ((reader.readLine().also { str = it }) != null) {
                builder.append(str).append("\n")
            }

            return builder.toString()
        } catch (e: Exception) {
            throw com.zeki.kisserver.exception.ApiException(ResponseCode.GOOGLE_CHAT_CONNECT_ERROR)
        } finally {
            conn?.disconnect()
        }
    }
}
