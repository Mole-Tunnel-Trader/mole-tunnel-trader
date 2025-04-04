package com.zeki.kisserver.domain.schduler

import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.common.em.ReportType
import com.zeki.holiday.service.HolidayDateService
import com.zeki.holiday.service.HolidayService
import com.zeki.kisserver.domain.data_go.stock_code.GetStockCodeService
import com.zeki.kisserver.domain.data_go.stock_code.StockCodeService
import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import com.zeki.kisserver.domain.kis.trade.TradeService
import com.zeki.report.DataReportService
import com.zeki.webhook.DiscordWebhookDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Scheduler(
        private val holidayService: HolidayService,
        private val holidayDateService: HolidayDateService,
        private val stockCodeService: StockCodeService,
        private val getStockCodeService: GetStockCodeService,
        private val dataReportService: DataReportService,
        private val objectMapper: ObjectMapper,
        private val stockInfoService: StockInfoService,
        private val stockPriceService: StockPriceService,
        private val asyncScheduler: AsyncScheduler,
        private val tradeService: TradeService
) {

    @Scheduled(cron = "0 30 7 * * *")
    fun updateHolidayAndStockCode() {
        val now = LocalDate.now()

        val upsertHoliday = holidayService.upsertHoliday(now.year)
        val upsertStockCode = stockCodeService.upsertStockCode()

        // report 내역 저장
        val discordWebhookDto =
                DiscordWebhookDto(
                        embeds =
                                listOf(
                                        DiscordWebhookDto.Embeds(
                                                fields =
                                                        listOf(
                                                                DiscordWebhookDto.Fields(
                                                                        name = "Data Go 일배치 Report",
                                                                        value =
                                                                                """
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
                startDateTime =
                        LocalDateTime.of(
                                /* date = */ LocalDate.now(),
                                /* time = */ LocalTime.of(9, 0)
                        ),
                content = content
        )
    }

    @Scheduled(cron = "0 1 18 * * *")
    fun updateStockInfo() {
        val stockCodeList = getStockCodeService.getStockCodeStringList()
        val upsertInfo = stockInfoService.upsertStockInfo(stockCodeList)

        // report 내역 저장
        val discordWebhookDto =
                DiscordWebhookDto(
                        embeds =
                                listOf(
                                        DiscordWebhookDto.Embeds(
                                                fields =
                                                        listOf(
                                                                DiscordWebhookDto.Fields(
                                                                        name = "주식정보 Report",
                                                                        value =
                                                                                """
                                **주식 정보**
                                - 신규: ${upsertInfo.newCount}
                                - 변경: ${upsertInfo.updateCount}
                                - 삭제: ${upsertInfo.deleteCount}
                            """.trimIndent()
                                                                )
                                                        )
                                        )
                                )
                )

        val content = objectMapper.writeValueAsString(discordWebhookDto)
        dataReportService.createDataReport(
                dateReport = ReportType.KIS,
                startDateTime =
                        LocalDateTime.of(
                                /* date = */ LocalDate.now(),
                                /* time = */ LocalTime.of(20, 0)
                        ),
                content = content
        )
    }

    @Scheduled(cron = "0 1 19 * * *")
    fun updateStockPrice() {
        val stockCodeList = getStockCodeService.getStockCodeStringList()
        val upsertPrice = stockPriceService.upsertStockPrice(stockCodeList, LocalDate.now(), 10)

        // report 내역 저장
        val discordWebhookDto =
                DiscordWebhookDto(
                        embeds =
                                listOf(
                                        DiscordWebhookDto.Embeds(
                                                fields =
                                                        listOf(
                                                                DiscordWebhookDto.Fields(
                                                                        name = "주식가격 Report",
                                                                        value =
                                                                                """
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
                startDateTime =
                        LocalDateTime.of(
                                /* date = */ LocalDate.now(),
                                /* time = */ LocalTime.of(20, 0)
                        ),
                content = content
        )

        asyncScheduler.updateRsi()
    }

    /** 매일 장 시작 직후(9시 1분)에 매매 처리 tradeQueue 테이블의 데이터를 처리하여 주식 매수 및 매도를 실행 */
    @Scheduled(cron = "0 1 9 * * *")
    fun processTradeQueue() {
        // 휴일인 경우 스킵
        if (holidayDateService.isHoliday(LocalDate.now())) {
            return
        }

        // tradeQueue 처리 실행
        tradeService.orderStockByTradeQueue()

        // 처리 결과 report 생성
        val discordWebhookDto =
                DiscordWebhookDto(
                        embeds =
                                listOf(
                                        DiscordWebhookDto.Embeds(
                                                fields =
                                                        listOf(
                                                                DiscordWebhookDto.Fields(
                                                                        name = "매매 처리 Report",
                                                                        value =
                                                                                "TradeQueue 처리가 완료되었습니다."
                                                                )
                                                        )
                                        )
                                )
                )

        val content = objectMapper.writeValueAsString(discordWebhookDto)
        dataReportService.createDataReport(
                dateReport = ReportType.KIS,
                startDateTime =
                        LocalDateTime.of(
                                /* date = */ LocalDate.now(),
                                /* time = */ LocalTime.of(9, 0)
                        ),
                content = content
        )
    }
}
