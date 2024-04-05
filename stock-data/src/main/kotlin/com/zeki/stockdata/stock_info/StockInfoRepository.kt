package com.zeki.stockdata.stock_info

import org.springframework.data.jpa.repository.JpaRepository

interface StockInfoRepository : JpaRepository<StockInfo, Long> {
    fun findByCodeIn(stockCodeList: List<String>): List<StockInfo>

    fun findByCode(code: String): StockInfo?
}