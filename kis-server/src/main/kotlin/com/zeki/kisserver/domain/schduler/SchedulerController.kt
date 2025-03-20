package com.zeki.kisserver.domain.schduler

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/scheduler")
class SchedulerController(
    private val scheduler: Scheduler
) {

    @GetMapping("/data-go")
    fun getDataGo() {
        scheduler.updateHolidayAndStockCode()
    }

    @GetMapping("/stock-info")
    fun getStockInfo() {
        scheduler.updateStockInfo()
    }

    @GetMapping("/stock-price")
    fun getStockPrice() {
        scheduler.updateStockPrice()
    }

}