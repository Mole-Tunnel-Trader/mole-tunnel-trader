package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.StockInfo
import org.springframework.data.jpa.repository.JpaRepository

interface StockInfoRepository : JpaRepository<StockInfo, Long> {
    fun findByCodeIn(stockCodeList: List<String>): List<StockInfo>

    fun findByCode(code: String): StockInfo?
}