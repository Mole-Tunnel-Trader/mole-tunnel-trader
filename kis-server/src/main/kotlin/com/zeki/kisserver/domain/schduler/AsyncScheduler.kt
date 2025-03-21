package com.zeki.kisserver.domain.schduler

import com.zeki.kisserver.domain.data_go.stock_code.GetStockCodeService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AsyncScheduler(
    private val getStockCodeService: GetStockCodeService,
    private val stockPriceService: StockPriceService
) {


    @Async
    fun updateRsi() {
        val stockCodeList = getStockCodeService.getStockCodeStringList()
        stockPriceService.updateRsi(stockCodeList, LocalDate.now())
    }
}