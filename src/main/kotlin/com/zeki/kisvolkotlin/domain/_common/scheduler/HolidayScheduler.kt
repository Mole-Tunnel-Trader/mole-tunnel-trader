package com.zeki.kisvolkotlin.domain._common.scheduler

import com.zeki.kisvolkotlin.domain.data_go.holiday.HolidayService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class HolidayScheduler(
    private val holidayService: HolidayService
) {

    //    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun updateHolidayAtOneDay() {
        holidayService.upsertHoliday()
    }

}