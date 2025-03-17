package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.StockPrice
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StockPriceRepository : JpaRepository<StockPrice, Long> {
    fun findByStockInfoCodeIn(stockCodeList: List<String>): List<StockPrice>

    fun findAllByDateAndStockInfoCodeIn(
            baseDate: LocalDate,
            stockCodeList: List<String>
    ): List<StockPrice>

    @Query(
            "SELECT DISTINCT sp FROM StockPrice sp JOIN FETCH sp.stockInfo WHERE sp.date >= :baseDate AND sp.stockInfo.code IN :stockCodeList ORDER BY sp.date ASC"
    )
    fun findAllByDateGreaterThanEqualAndStockInfoCodeInWithStockInfoOrderByDateAsc(
            baseDate: LocalDate,
            stockCodeList: List<String>
    ): List<StockPrice>

    fun findAllByDateGreaterThanEqualAndStockInfoCodeInOrderByDateAsc(
            baseDate: LocalDate,
            stockCodeList: List<String>
    ): List<StockPrice>

    @Query(
            "SELECT DISTINCT sp FROM StockPrice sp JOIN FETCH sp.stockInfo WHERE sp.date BETWEEN :startDate AND :endDate AND sp.stockInfo.code IN :stockCodeList"
    )
    fun findAllByDateBetweenAndStockInfoCodeInWithStockInfo(
            startDate: LocalDate,
            endDate: LocalDate,
            stockCodeList: List<String>
    ): List<StockPrice>

    fun findAllByDateBetweenAndStockInfoCodeIn(
            startDate: LocalDate,
            endDate: LocalDate,
            stockCodeList: List<String>
    ): List<StockPrice>

    @Query(
            "SELECT DISTINCT sp FROM StockPrice sp JOIN FETCH sp.stockInfo WHERE sp.stockInfo.code IN :stockCodeList AND sp.date = :date"
    )
    fun findAllByStockInfo_CodeInAndDateWithStockInfo(
            stockCodeList: List<String>,
            date: LocalDate
    ): List<StockPrice>

    fun findAllByStockInfo_CodeInAndDate(
            stockCodeList: List<String>,
            date: LocalDate
    ): List<StockPrice>
}
