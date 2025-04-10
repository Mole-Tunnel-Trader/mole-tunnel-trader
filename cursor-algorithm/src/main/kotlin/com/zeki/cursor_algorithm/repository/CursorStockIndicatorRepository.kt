package com.zeki.cursor_algorithm.repository

import com.zeki.cursor_algorithm.entity.CursorStockIndicator
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface CursorStockIndicatorRepository : JpaRepository<CursorStockIndicator, Long> {

    /** 지정된 날짜의 특정 종목에 대한 지표 조회 */
    fun findByStockCodeAndDate(stockCode: String, date: LocalDate): CursorStockIndicator?

    /** 지정된 날짜의 모든 종목 지표 조회 */
    fun findByDate(date: LocalDate): List<CursorStockIndicator>

    /** 지정된 날짜 범위 내의 특정 종목 지표 조회 */
    fun findByStockCodeAndDateBetweenOrderByDateAsc(
        stockCode: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CursorStockIndicator>

    /** 특정 거래량 비율 이상인 종목 조회 */
    fun findByDateAndVolumeRatioGreaterThanEqualOrderByVolumeRatioDesc(
        date: LocalDate,
        volumeRatio: java.math.BigDecimal
    ): List<CursorStockIndicator>

    /** 특정 날짜에 거래량이 급증한 종목 조회 (거래량 비율이 높고, 가격 변동률이 양수인 종목) */
    @Query(
        "SELECT c FROM CursorStockIndicator c WHERE c.date = :date AND c.volumeRatio >= :minVolumeRatio AND c.priceChangeRate > 0 ORDER BY c.volumeRatio DESC"
    )
    fun findVolumeSpikesWithPriceIncrease(
        date: LocalDate,
        minVolumeRatio: java.math.BigDecimal
    ): List<CursorStockIndicator>

    /** 특정 날짜에 거래량이 급증했지만 가격이 하락한 종목 조회 */
    @Query(
        "SELECT c FROM CursorStockIndicator c WHERE c.date = :date AND c.volumeRatio >= :minVolumeRatio AND c.priceChangeRate < 0 ORDER BY c.volumeRatio DESC"
    )
    fun findVolumeSpikesWithPriceDecrease(
        date: LocalDate,
        minVolumeRatio: java.math.BigDecimal
    ): List<CursorStockIndicator>

    /** 특정 날짜에 거래량 변동성이 큰 종목 조회 (거래량 비율과 변동성이 모두 높은 종목) */
    @Query(
        "SELECT c FROM CursorStockIndicator c WHERE c.date = :date AND c.volumeRatio >= :minVolumeRatio AND c.volatility >= :minVolatility ORDER BY (c.volumeRatio + c.volatility) DESC"
    )
    fun findHighVolatilityWithVolumeSpikes(
        date: LocalDate,
        minVolumeRatio: java.math.BigDecimal,
        minVolatility: java.math.BigDecimal
    ): List<CursorStockIndicator>
}
