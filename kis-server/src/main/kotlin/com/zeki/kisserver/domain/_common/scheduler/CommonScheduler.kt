package com.zeki.kisserver.domain._common.scheduler

import com.zeki.exception.ExceptionUtils.log
import com.zeki.kisserver.domain.data_go.holiday.HolidayDateService
import com.zeki.kisserver.domain.data_go.holiday.HolidayService
import com.zeki.kisserver.domain.data_go.stock_code.StockCodeService
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

    private val jdbcTemplate: JdbcTemplate
) {

    @Scheduled(fixedRate = 60000 * 60)
    fun connection() {
        jdbcTemplate.execute("SELECT 1")
        log.info("DB Connection Check")
    }

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    fun updateHolidayByDaily() {
        if (!holidayDateService.isHoliday(LocalDate.now())) {
            stockCodeService.upsertStockCode()
            holidayService.upsertHoliday()
        }
    }

}