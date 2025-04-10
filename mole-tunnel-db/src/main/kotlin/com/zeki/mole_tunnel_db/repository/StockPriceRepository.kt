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

    fun findAllByStockInfo_CodeInAndDate(
        stockCodeList: List<String>,
        date: LocalDate
    ): List<StockPrice>

    fun findByStockInfo_CodeAndDate(stockCode: String, date: LocalDate): StockPrice?

    /** 특정 날짜의 모든 주식 가격 데이터를 조회하는 메소드 종목 코드 기준으로 정렬 */
    fun findAllByDateOrderByStockInfoCode(date: LocalDate): List<StockPrice>

    /** 특정 날짜의 모든 주식 가격 데이터를 조회하는 메소드 */
    fun findAllByDate(date: LocalDate): List<StockPrice>
}
