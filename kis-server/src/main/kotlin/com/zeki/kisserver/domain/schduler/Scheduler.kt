package com.zeki.kisserver.domain.schduler

import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.common.em.ReportType
import com.zeki.holiday.service.HolidayService
import com.zeki.kisserver.domain.data_go.stock_code.StockCodeService
import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import com.zeki.report.DataReportService
import com.zeki.webhook.DiscordWebhookDto
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@Component
class Scheduler(
    private val holidayService: HolidayService,
    private val stockCodeService: StockCodeService,
    private val dataReportService: DataReportService,
    private val objectMapper: ObjectMapper,
    private val stockInfoService: StockInfoService,
    private val stockPriceService: StockPriceService,
) {

    @Scheduled(cron = "0 30 7 * * *")
    fun updateHolidayAndStockCode() {
        val now = LocalDate.now()

        val upsertHoliday = holidayService.upsertHoliday(now.year)
        val upsertStockCode = stockCodeService.upsertStockCode()

        // report 내역 저장
        val discordWebhookDto = DiscordWebhookDto(
            embeds = listOf(
                DiscordWebhookDto.Embeds(
                    fields = listOf(
                        DiscordWebhookDto.Fields(
                            name = "Data Go 일배치 Report",
                            value = """
                                **주식코드**
                                - 신규: ${upsertStockCode.newCount}
                                - 변경: ${upsertStockCode.updateCount}
                                - 삭제: ${upsertStockCode.deleteCount}
                                
                                **휴일**
                                - 신규: ${upsertHoliday.newCount}
                                - 변경: ${upsertHoliday.updateCount}
                                - 삭제: ${upsertHoliday.deleteCount}
                            """.trimIndent()
                        )
                    )
                )
            )
        )

        val content = objectMapper.writeValueAsString(discordWebhookDto)
        dataReportService.createDataReport(
            dateReport = ReportType.DATA_GO,
            startDateTime = LocalDateTime.of(
                /* date = */ LocalDate.now(),
                /* time = */ LocalTime.of(9, 0)
            ),
            content = content
        )
    }

    @Scheduled(cron = "0 * * * * *")
    fun sendReportWebhook() {
        val now = LocalDateTime.now()
        dataReportService.sendDataReport(now)
    }

    @Scheduled(cron = "0 0 19 * * *")
    fun updateStockInfoAndStockPrice() {
        val stockCodeList = stockCodeService.getStockCodeList()
        val upsertInfo = stockInfoService.upsertStockInfo(stockCodeList)
        val upsertPrice = stockPriceService.upsertStockPrice(stockCodeList, LocalDate.now(), 5)

        // report 내역 저장
        val discordWebhookDto = DiscordWebhookDto(
            embeds = listOf(
                DiscordWebhookDto.Embeds(
                    fields = listOf(
                        DiscordWebhookDto.Fields(
                            name = "주식정보 Report",
                            value = """
                                **주식 정보**
                                - 신규: ${upsertInfo.newCount}
                                - 변경: ${upsertInfo.updateCount}
                                - 삭제: ${upsertInfo.deleteCount}
                                
                                **주식 가격**
                                - 신규: ${upsertPrice.newCount}
                                - 변경: ${upsertPrice.updateCount}
                                - 삭제: ${upsertPrice.deleteCount}
                            """.trimIndent()
                        )
                    )
                )
            )
        )

        val content = objectMapper.writeValueAsString(discordWebhookDto)
        dataReportService.createDataReport(
            dateReport = ReportType.KIS,
            startDateTime = LocalDateTime.of(
                /* date = */ LocalDate.now(),
                /* time = */ LocalTime.of(21, 0)
            ),
            content = content
        )
    }
}