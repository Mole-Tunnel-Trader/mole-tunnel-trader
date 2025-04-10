package com.zeki.kisserver.domain.schduler

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/scheduler")
class SchedulerController(
    private val scheduler: Scheduler,
    private val asyncScheduler: AsyncScheduler,
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

    @GetMapping("/rsi")
    fun getRsi() {
        asyncScheduler.updateRsi(LocalDate.now())
    }

    @GetMapping("/volume-indicators")
    fun getVolumeIndicators() {
        asyncScheduler.updateVolumeIndicators(LocalDate.now())
    }

}