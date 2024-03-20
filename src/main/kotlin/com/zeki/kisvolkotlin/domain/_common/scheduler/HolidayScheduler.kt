package com.zeki.kisvolkotlin.domain._common.scheduler

import com.zeki.kisvolkotlin.domain.data_go.holiday.HolidayDateService
import com.zeki.kisvolkotlin.domain.data_go.holiday.HolidayService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class HolidayScheduler(
    private val holidayService: HolidayService,
    private val holidayDateService: HolidayDateService
) {

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    fun updateHolidayByDaily() {
        if (!holidayDateService.isHoliday(LocalDate.now())) {
            holidayService.upsertHoliday()
        }
    }

}