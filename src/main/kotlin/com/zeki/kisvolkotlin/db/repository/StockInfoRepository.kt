package com.zeki.kisvolkotlin.db.repository

import com.zeki.kisvolkotlin.db.entity.StockInfo
import org.springframework.data.jpa.repository.JpaRepository

interface StockInfoRepository : JpaRepository<StockInfo, Long> {
    fun findByCodeIn(stockCodeList: List<String>): List<StockInfo>
}