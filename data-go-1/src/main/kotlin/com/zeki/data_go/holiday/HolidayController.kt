package com.zeki.data_go.holiday

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HolidayController(
    private val holidayService: HolidayService
) {

    @GetMapping("/holiday/update")
    fun updateHoliday() {
        holidayService.upsertHoliday()
    }
}