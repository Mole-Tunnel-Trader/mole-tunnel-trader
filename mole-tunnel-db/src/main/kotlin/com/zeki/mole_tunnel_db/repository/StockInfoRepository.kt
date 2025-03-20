package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.StockInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StockInfoRepository : JpaRepository<StockInfo, Long> {
    fun findByCodeIn(stockCodeList: List<String>): List<StockInfo>

    @Query(
            "SELECT DISTINCT si FROM StockInfo si LEFT JOIN FETCH si.stockPriceList sp " +
                    "WHERE si.code IN :stockCodeList " +
                    "AND sp.id IN (SELECT sp2.id FROM StockPrice sp2 WHERE sp2.stockInfo.id = si.id " +
                    "ORDER BY sp2.date DESC LIMIT 30)"
    )
    fun findByCodeInWithStockPrices(stockCodeList: List<String>): List<StockInfo>

    fun findByCode(code: String): StockInfo?
}
