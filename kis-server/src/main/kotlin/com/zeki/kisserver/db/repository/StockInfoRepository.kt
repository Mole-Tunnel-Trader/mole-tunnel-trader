package com.zeki.kisserver.db.repository

import com.zeki.db.entity.StockInfo
import com.zeki.kisserver.db.entity.StockInfo
import com.zeki.kisvolkotlin.db.entity.StockInfo
import org.springframework.data.jpa.repository.JpaRepository

interface StockInfoRepository : JpaRepository<StockInfo, Long> {
    fun findByCodeIn(stockCodeList: List<String>): List<StockInfo>

    fun findByCode(code: String): StockInfo?
}