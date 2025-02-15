package com.zeki.mole_tunnel_db.entity

import com.zeki.common.entity.BaseEntity
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
    rsi: Float? = null
) : BaseEntity() {

    @Column(name = "date", nullable = false)
    @Comment("일자")
    var date: LocalDate = date
        protected set

    @Column(name = "close", nullable = false, precision = 12)
    @Comment("종가")
    var close: BigDecimal = close
        protected set

    @Column(name = "open", nullable = false, precision = 12)
    @Comment("시가")
    var open: BigDecimal = open
        protected set

    @Column(name = "high", nullable = false, precision = 12)
    @Comment("고가")
    var high: BigDecimal = high
        protected set

    @Column(name = "low", nullable = false, precision = 12)
    @Comment("저가")
    var low: BigDecimal = low
        protected set

    @Column(name = "volume", nullable = false)
    @Comment("거래량")
    var volume: Long = volume
        protected set

    @Column(name = "rsi", nullable = true, precision = 12)
    @Comment("RSI")
    var rsi: Float? = rsi

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
            stockInfo: StockInfo
        ): StockPrice {
            val stockPrice = StockPrice(
                date = date,
                close = close,
                open = open,
                high = high,
                low = low,
                volume = volume,
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
        volume: Long
    ): Boolean {
        if (this.close == close &&
            this.open == open &&
            this.high == high &&
            this.low == low &&
            this.volume == volume
        ) return false

        this.close = close
        this.open = open
        this.high = high
        this.low = low
        this.volume = volume

        return true
    }

}