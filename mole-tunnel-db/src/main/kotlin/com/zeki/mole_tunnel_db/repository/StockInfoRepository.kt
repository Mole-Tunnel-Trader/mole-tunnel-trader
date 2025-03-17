package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.StockInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StockInfoRepository : JpaRepository<StockInfo, Long> {
    fun findByCodeIn(stockCodeList: List<String>): List<StockInfo>

    @Query(
            "SELECT DISTINCT si FROM StockInfo si LEFT JOIN FETCH si.stockPriceList WHERE si.code IN :stockCodeList"
    )
    fun findByCodeInWithStockPrices(stockCodeList: List<String>): List<StockInfo>

    fun findByCode(code: String): StockInfo?
}
