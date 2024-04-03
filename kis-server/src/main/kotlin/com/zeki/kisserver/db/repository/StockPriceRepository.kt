package com.zeki.kisserver.db.repository

import com.zeki.db.entity.StockPrice
import com.zeki.kisserver.db.entity.StockPrice
import com.zeki.kisvolkotlin.db.entity.StockPrice
import org.springframework.data.jpa.repository.JpaRepository

interface StockPriceRepository : JpaRepository<StockPrice, Long> {
    fun findByStockInfoCodeIn(stockCodeList: List<String>): List<StockPrice>
}