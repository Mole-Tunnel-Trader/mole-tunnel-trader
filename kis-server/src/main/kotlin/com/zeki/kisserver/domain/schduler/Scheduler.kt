package com.zeki.kisserver.domain.schduler

import com.fasterxml.jackson.databind.ObjectMapper
import com.zeki.common.em.ReportType
import com.zeki.holiday.service.HolidayDateService
import com.zeki.holiday.service.HolidayService
import com.zeki.kisserver.domain.kis.trade.TradeService
import com.zeki.report.DataReportService
import com.zeki.stockcode.service.GetStockCodeService
import com.zeki.stockcode.service.StockCodeService
import com.zeki.stockdata.service.stock_info.StockInfoService
import com.zeki.stockdata.service.stock_price.StockPriceService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime

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

        // 맵을 이용한 report 내역 저장 - 카테고리별로 구분됨
        val reportMap =
            mapOf(
                "주식코드 신규" to upsertStockCode.newCount,
                "주식코드 변경" to upsertStockCode.updateCount,
                "주식코드 삭제" to upsertStockCode.deleteCount,
                "휴일 신규" to upsertHoliday.newCount,
                "휴일 변경" to upsertHoliday.updateCount,
                "휴일 삭제" to upsertHoliday.deleteCount
            )

        dataReportService.createReportFromMap(
            reportType = ReportType.DATA_GO,
            reportName = "Data Go 일배치 Report",
            reportMap = reportMap
        )
    }

    @Scheduled(cron = "0 1 19 * * *")
    fun updateStockInfo() {
        val stockCodeList = getStockCodeService.getStockCodeStringList()
        val upsertInfo = stockInfoService.upsertStockInfo(stockCodeList)

        // 맵을 이용한 report 내역 저장
        val reportMap =
            mapOf(
                "주식정보 신규" to upsertInfo.newCount,
                "주식정보 변경" to upsertInfo.updateCount,
                "주식정보 삭제" to upsertInfo.deleteCount
            )

        dataReportService.createReportFromMap(
            reportType = ReportType.KIS,
            reportName = "주식정보 Report",
            reportMap = reportMap,
            time = LocalTime.of(21, 1)
        )
    }

    @Scheduled(cron = "0 1 20 * * *")
    fun updateStockPrice() {
        val stockCodeList = getStockCodeService.getStockCodeStringList()
        val now = LocalDate.now()
        val upsertPrice = stockPriceService.upsertStockPrice(stockCodeList, now, 10)

        // 맵을 이용한 report 내역 저장
        val reportMap =
            mapOf(
                "주식가격 신규" to upsertPrice.newCount,
                "주식가격 변경" to upsertPrice.updateCount,
                "주식가격 삭제" to upsertPrice.deleteCount
            )

        dataReportService.createReportFromMap(
            reportType = ReportType.KIS,
            reportName = "주식가격 Report",
            reportMap = reportMap,
            time = LocalTime.of(21, 1)
        )

        asyncScheduler.updateRsi(now)
        asyncScheduler.updateVolumeIndicators(now)
    }

    /** 매일 장 시작 직후(8시 50분)에 매매 처리 tradeQueue 테이블의 데이터를 처리하여 주식 매수 및 매도를 실행 */
    @Scheduled(cron = "0 50 8 * * *")
    fun processTradeQueue() {
        // 휴일인 경우 스킵
        if (holidayDateService.isHoliday(LocalDate.now())) {
            return
        }
        // tradeQueue 처리 실행
        tradeService.orderStockByTradeQueue()

        // 맵을 이용한 report 내역 저장
        val reportMap = mapOf("매매 상태" to "완료됨")

        dataReportService.createReportFromMap(
            reportType = ReportType.KIS,
            reportName = "매매 처리 Report",
            reportMap = reportMap
        )
    }
}
