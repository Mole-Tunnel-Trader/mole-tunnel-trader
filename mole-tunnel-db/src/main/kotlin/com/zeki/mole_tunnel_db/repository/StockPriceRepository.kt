package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.StockPrice
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface StockPriceRepository : JpaRepository<StockPrice, Long> {
    fun findByStockInfoCodeIn(stockCodeList: List<String>): List<StockPrice>

    fun findAllByDateAndStockInfoCodeIn(
        baseDate: LocalDate,
        stockCodeList: List<String>
    ): List<StockPrice>

    fun findAllByDateGreaterThanEqualAndStockInfoCodeInOrderByDateAsc(
        baseDate: LocalDate,
        stockCodeList: List<String>
    ): List<StockPrice>

    fun findAllByDateBetweenAndStockInfoCodeIn(
        startDate: LocalDate,
        endDate: LocalDate,
        stockCodeList: List<String>
    ): List<StockPrice>

    fun findAllByStockCodeInAndDate(stockCodeList: List<String>, date: LocalDate): List<StockPrice>
}