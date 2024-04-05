package com.zeki.stockdata.stock_price

import org.springframework.data.jpa.repository.JpaRepository

interface StockPriceRepository : JpaRepository<StockPrice, Long> {
    fun findByStockInfoCodeIn(stockCodeList: List<String>): List<StockPrice>
}