package com.zeki.cursor_algorithm.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate

/** 커서 알고리즘에서 사용하는 주식 지표 엔티티 거래량 관련 지표를 저장합니다. */
@Entity
@Table(
    name = "cursor_stock_indicator",
    indexes =
        [
            Index(
                name = "idx_cursor_stock_indicator_stock_code",
                columnList = "stock_code"
            ),
            Index(name = "idx_cursor_stock_indicator_date", columnList = "date")]
)
class CursorStockIndicator(
    stockCode: String,
    date: LocalDate,
    volume: Long,
    volumeAvg5: BigDecimal,
    volumeAvg20: BigDecimal,
    volumeRatio: BigDecimal,
    priceChangeRate: BigDecimal,
    volatility: BigDecimal
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(name = "stock_code", nullable = false, length = 20)
    @Comment("종목코드")
    var stockCode: String = stockCode
        protected set

    @Column(name = "date", nullable = false)
    @Comment("날짜")
    var date: LocalDate = date
        protected set

    @Column(name = "volume", nullable = false)
    @Comment("거래량")
    var volume: Long = volume
        protected set

    @Column(name = "volume_avg_5", nullable = false)
    @Comment("5일 평균 거래량")
    var volumeAvg5: BigDecimal = volumeAvg5
        protected set

    @Column(name = "volume_avg_20", nullable = false)
    @Comment("20일 평균 거래량")
    var volumeAvg20: BigDecimal = volumeAvg20
        protected set

    @Column(name = "volume_ratio", nullable = false)
    @Comment("거래량 비율 (현재 거래량 / 20일 평균 거래량)")
    var volumeRatio: BigDecimal = volumeRatio
        protected set

    @Column(name = "price_change_rate", nullable = false)
    @Comment("가격 변동률 (%)")
    var priceChangeRate: BigDecimal = priceChangeRate
        protected set

    @Column(name = "volatility", nullable = false)
    @Comment("변동성 (고가 - 저가) / 시가")
    var volatility: BigDecimal = volatility
        protected set

    companion object {
        fun create(
            stockCode: String,
            date: LocalDate,
            volume: Long,
            volumeAvg5: BigDecimal,
            volumeAvg20: BigDecimal,
            volumeRatio: BigDecimal,
            priceChangeRate: BigDecimal,
            volatility: BigDecimal
        ): CursorStockIndicator {
            return CursorStockIndicator(
                stockCode = stockCode,
                date = date,
                volume = volume,
                volumeAvg5 = volumeAvg5,
                volumeAvg20 = volumeAvg20,
                volumeRatio = volumeRatio,
                priceChangeRate = priceChangeRate,
                volatility = volatility
            )
        }
    }

    /** 데이터 업데이트 함수 */
    fun updateIndicator(
        volume: Long,
        volumeAvg5: BigDecimal,
        volumeAvg20: BigDecimal,
        volumeRatio: BigDecimal,
        priceChangeRate: BigDecimal,
        volatility: BigDecimal
    ): Boolean {
        if (this.volume == volume &&
            this.volumeAvg5 == volumeAvg5 &&
            this.volumeAvg20 == volumeAvg20 &&
            this.volumeRatio == volumeRatio &&
            this.priceChangeRate == priceChangeRate &&
            this.volatility == volatility
        )
            return false

        this.volume = volume
        this.volumeAvg5 = volumeAvg5
        this.volumeAvg20 = volumeAvg20
        this.volumeRatio = volumeRatio
        this.priceChangeRate = priceChangeRate
        this.volatility = volatility

        return true
    }
}
