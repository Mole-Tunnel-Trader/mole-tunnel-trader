package com.zeki.stockdata.stock_price

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface StockPriceRepository : JpaRepository<StockPrice, Long> {
    fun findByStockInfoCodeIn(stockCodeList: List<String>): List<StockPrice>

    fun findAllByDateAndStockInfoCodeIn(
        baseDate: LocalDate,
        stockCodeList: List<String>
    ): List<StockPrice>
}