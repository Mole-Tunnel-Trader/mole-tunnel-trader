package com.zeki.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.common.em.ReportType
import com.zeki.mole_tunnel_db.entity.DataReport
import com.zeki.mole_tunnel_db.repository.DataReportRepository
import com.zeki.ok_http_client.ApiStatics
import com.zeki.ok_http_client.OkHttpClientConnector
import com.zeki.webhook.DiscordWebhookDto
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class DataReportService(
    private val dataReportRepository: DataReportRepository,
    private val okHttpClientConnector: OkHttpClientConnector,
    private val objectMapper: ObjectMapper,
    private val apiStatics: ApiStatics
) {

    @Transactional
    fun createDataReport(dateReport: ReportType, startDateTime: LocalDateTime, content: String) {
        val data = DataReport.create(
            dateReport,
            apiStatics.webhook.reportUrl,
            startDateTime,
            content
        )

        dataReportRepository.save(data)
    }

    @Transactional
    fun sendDataReport(now: LocalDateTime) {
        val startDateTime = now.withSecond(0)
        val endDateTime = now.withSecond(59)
        val dataReports =
            dataReportRepository.findByReportDateTimeBetween(startDateTime, endDateTime)
        dataReports.forEach {
            val reqBody = objectMapper.readValue(it.content, DiscordWebhookDto::class.java)
            okHttpClientConnector.connect(
                clientType = OkHttpClientConnector.ClientType.DEFAULT,
                method = HttpMethod.POST,
                path = it.url,
                requestBody = reqBody,
                responseClassType = Unit::class.java,
            )
        }

    }
}