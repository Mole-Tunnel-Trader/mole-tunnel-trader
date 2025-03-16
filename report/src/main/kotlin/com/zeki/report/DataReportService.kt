package com.zeki.report

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.common.em.ReportType
import com.zeki.common.exception.ExceptionUtils
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
            apiStatics.discord.reportUrl,
            startDateTime,
            content
        )

        dataReportRepository.save(data)
    }

    @Transactional
    fun sendDataReport(now: LocalDateTime) {
        val startDateTime = now.withSecond(0).withNano(0)
        val endDateTime = now.withSecond(59)
        val dataReports =
            dataReportRepository.findByReportDateTimeBetweenAndName(
                startDateTime,
                endDateTime,
                ReportType.DATA_GO
            )

        dataReports.forEach {
            ExceptionUtils.log.info { "sendDataReport: $it" }
            val reqBody = objectMapper.readValue(it.content, DiscordWebhookDto::class.java)
            val connect = okHttpClientConnector.connect(
                clientType = OkHttpClientConnector.ClientType.DEFAULT,
                method = HttpMethod.POST,
                path = it.url,
                requestBody = reqBody,
                responseClassType = JsonNode::class.java,
            )
            //TODO: response 확인 후 isSend 값 변경
            it.isSend = "Y"
        }

    }
}