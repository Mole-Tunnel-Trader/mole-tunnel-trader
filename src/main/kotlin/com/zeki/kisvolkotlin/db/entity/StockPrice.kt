package com.zeki.kisvolkotlin.db.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "stock_price")
class StockPrice private constructor(
    date: LocalDate,
    close: BigDecimal,
    open: BigDecimal,
    high: BigDecimal,
    low: BigDecimal,
    volume: Long,
    volumeTotalPrice: BigDecimal,
    beforeGapPrice: BigDecimal,
    beforeGapSign: BigDecimal,
) : BaseEntity() {

    @Column(name = "date", nullable = false)
    @Comment("일자")
    var date: LocalDate = date
        protected set

    @Column(name = "close", nullable = false)
    @Comment("종가")
    var close: BigDecimal = close
        protected set

    @Column(name = "open", nullable = false)
    @Comment("시가")
    var open: BigDecimal = open
        protected set

    @Column(name = "high", nullable = false)
    @Comment("고가")
    var high: BigDecimal = high
        protected set

    @Column(name = "low", nullable = false)
    @Comment("저가")
    var low: BigDecimal = low
        protected set

    @Column(name = "volume", nullable = false)
    @Comment("거래량")
    var volume: Long = volume
        protected set

    @Column(name = "volume_total_price", nullable = false)
    @Comment("거래대금")
    var volumeTotalPrice: BigDecimal = volumeTotalPrice
        protected set

    @Column(name = "before_gap_price", nullable = false)
    @Comment("전일대비가격")
    var beforeGapPrice: BigDecimal = beforeGapPrice
        protected set

    @Column(name = "before_gap_sign", nullable = false)
    @Comment("전일대비부호")
    var beforeGapSign: BigDecimal = beforeGapSign
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("주식정보 ID")
    lateinit var stockInfo: StockInfo
        protected set

    companion object {
        fun create(
            date: LocalDate,
            close: BigDecimal,
            open: BigDecimal,
            high: BigDecimal,
            low: BigDecimal,
            volume: Long,
            volumeTotalPrice: BigDecimal,
            beforeGapPrice: BigDecimal,
            beforeGapSign: BigDecimal,
            stockInfo: StockInfo
        ): StockPrice {
            val stockPrice = StockPrice(
                date = date,
                close = close,
                open = open,
                high = high,
                low = low,
                volume = volume,
                volumeTotalPrice = volumeTotalPrice,
                beforeGapPrice = beforeGapPrice,
                beforeGapSign = beforeGapSign
            )
            stockInfo.addStockPrice(stockPrice)

            return stockPrice
        }
    }

    fun regStockInfo(stockInfo: StockInfo) {
        this.stockInfo = stockInfo
    }

    fun updateStockPrice(
        close: BigDecimal,
        open: BigDecimal,
        high: BigDecimal,
        low: BigDecimal,
        volume: Long,
        volumeTotalPrice: BigDecimal,
        beforeGapPrice: BigDecimal,
        beforeGapSign: BigDecimal
    ): Boolean {
        if (this.close == close &&
            this.open == open &&
            this.high == high &&
            this.low == low &&
            this.volume == volume &&
            this.volumeTotalPrice == volumeTotalPrice &&
            this.beforeGapPrice == beforeGapPrice &&
            this.beforeGapSign == beforeGapSign
        ) return false

        this.close = close
        this.open = open
        this.high = high
        this.low = low
        this.volume = volume
        this.volumeTotalPrice = volumeTotalPrice
        this.beforeGapPrice = beforeGapPrice
        this.beforeGapSign = beforeGapSign

        return true
    }

}