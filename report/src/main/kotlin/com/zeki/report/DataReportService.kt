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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class DataReportService(
    private val dataReportRepository: DataReportRepository,
    private val okHttpClientConnector: OkHttpClientConnector,
    private val objectMapper: ObjectMapper,
    private val apiStatics: ApiStatics
) {

    @Transactional
    fun createDataReport(dateReport: ReportType, startDateTime: LocalDateTime, content: String) {
        val data =
            DataReport.create(dateReport, apiStatics.discord.reportUrl, startDateTime, content)

        dataReportRepository.save(data)
    }

    /**
     * 맵을 기반으로 디스코드 웹훅 리포트를 생성합니다.
     * @param reportType 보고서 유형
     * @param reportName 보고서 이름
     * @param reportMap 보고서 데이터 맵. 키-값 쌍으로 구성되며, 값은 숫자나 문자열일 수 있습니다.
     * @param date 보고서 날짜 (기본값: 오늘)
     * @param time 보고서 시간 (기본값: 9시)
     */
    @Transactional
    fun createReportFromMap(
        reportType: ReportType,
        reportName: String,
        reportMap: Map<String, Any>,
        date: LocalDate = LocalDate.now(),
        time: LocalTime = LocalTime.of(9, 0)
    ) {
        // 맵 데이터를 문자열로 변환
        val valueText = formatReport(reportMap)

        // 디스코드 웹훅 DTO 생성
        val discordWebhookDto =
            DiscordWebhookDto(
                embeds =
                    listOf(
                        DiscordWebhookDto.Embeds(
                            fields =
                                listOf(
                                    DiscordWebhookDto.Fields(
                                        name = reportName,
                                        value = valueText
                                    )
                                )
                        )
                    )
            )

        // JSON 직렬화
        val content = objectMapper.writeValueAsString(discordWebhookDto)

        // 리포트 저장
        createDataReport(
            dateReport = reportType,
            startDateTime = LocalDateTime.of(date, time),
            content = content
        )
    }

    /**
     * 리포트 데이터 맵을 포맷팅하여 보기 좋은 문자열로 변환합니다.
     *
     * @param reportMap 리포트 데이터 맵
     * @return 포맷팅된 문자열
     */
    private fun formatReport(reportMap: Map<String, Any>): String {
        // 카테고리별로 그룹화
        val groupedMap = mutableMapOf<String, MutableList<Pair<String, Any>>>()

        for ((key, value) in reportMap) {
            // 카테고리와 항목 분리
            val parts = key.split(" ", limit = 2)
            val category = if (parts.size > 1) parts[0] else "기본"
            val itemName = if (parts.size > 1) parts[1] else key

            // 카테고리별로 항목 그룹화
            if (!groupedMap.containsKey(category)) {
                groupedMap[category] = mutableListOf()
            }
            groupedMap[category]?.add(itemName to value)
        }

        // 각 카테고리별로 포맷팅
        return buildString {
            for ((category, items) in groupedMap) {
                if (groupedMap.size > 1) {
                    append("**$category**\n")
                }

                for ((itemName, value) in items) {
                    append("- $itemName: $value\n")
                }

                if (groupedMap.size > 1) {
                    append("\n")
                }
            }
        }
            .trimEnd()
    }

    @Transactional
    fun sendDataReport(now: LocalDateTime) {
        val startDateTime = now.withSecond(0).withNano(0)
        val endDateTime = now.withSecond(59)
        val dataReports =
            dataReportRepository.findByReportDateTimeBetween(startDateTime, endDateTime)

        dataReports.forEach {
            ExceptionUtils.log.info { "sendDataReport: $it" }
            val reqBody = objectMapper.readValue(it.content, DiscordWebhookDto::class.java)
            okHttpClientConnector.connect(
                clientType = OkHttpClientConnector.ClientType.DEFAULT,
                method = HttpMethod.POST,
                path = it.url,
                requestBody = reqBody,
                responseClassType = JsonNode::class.java,
            )

            it.isSend = "Y"
        }
    }
}
