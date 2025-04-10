package com.zeki.back_test_server.controller

import com.zeki.back_test_server.service.BackTestService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDate

@RestController
class BackTestController(
    private val backTestService: BackTestService,
) {

    @GetMapping("/backtest")
    fun backTest(): Unit {
        backTestService.backTest(2L, LocalDate.now().minusYears(1), LocalDate.now(), BigDecimal(10_000_000))
    }
}