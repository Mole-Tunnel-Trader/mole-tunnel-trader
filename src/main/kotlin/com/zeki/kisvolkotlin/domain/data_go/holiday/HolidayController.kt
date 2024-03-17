package com.zeki.kisvolkotlin.domain.data_go.holiday

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class HolidayController(
    private val holidayService: HolidayService
) {

    @GetMapping("/holiday")
    fun getHoliday() {
        holidayService.isHoliday(LocalDate.now())
    }

    @GetMapping("/holiday/update")
    fun updateHoliday() {
        holidayService.updateHoliday()
    }
}