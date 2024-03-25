package com.zeki.kisvolkotlin.domain.kis.stock_info

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StockInfoServiceTest(
    @Autowired private var stockInfoService: StockInfoService
) {
    @Test
    fun test() {
        stockInfoService.upsertStockInfo(listOf("000020", "000040"))
    }
}