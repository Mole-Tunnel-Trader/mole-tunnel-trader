package com.zeki.kisserver.domain._common.scheduler

import com.zeki.common.exception.ExceptionUtils
import com.zeki.kisserver.domain.data_go.holiday.HolidayDateService
import com.zeki.kisserver.domain.data_go.holiday.HolidayService
import com.zeki.kisserver.domain.data_go.stock_code.StockCodeService
import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class CommonScheduler(
    private val holidayService: HolidayService,
    private val holidayDateService: HolidayDateService,
    private val stockCodeService: StockCodeService,
    private val stockPriceService: StockPriceService,
    private val stockInfoService: StockInfoService,

    private val jdbcTemplate: JdbcTemplate
) {

    @Scheduled(fixedRate = 60000 * 60)
    fun connection() {
        jdbcTemplate.execute("SELECT 1")
        ExceptionUtils.log.info("Connection Check")
    }

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    fun updateHolidayByDaily() {

        holidayService.upsertHoliday()
//        if (!holidayDateService.isHoliday(LocalDate.now())) {
//            stockCodeService.upsertStockCode()
//        }
    }

    //    @Scheduled(cron = "0 0 23 * * *")
//    @Transactional
    fun upsertStockData() {
//        val nowDate = LocalDate.now()

//        stockPriceService.upsertStockPrice(listOf("000020"), nowDate, 10)

//        stockInfoService.upsertStockInfo(stockCodeList)
//        stockPriceService.upsertStockPrice(stockCodeList, nowDate, 3000)
        stockPriceService.updateRsi(listOf("000020"), LocalDate.of(2000, 1, 1))

    }
}