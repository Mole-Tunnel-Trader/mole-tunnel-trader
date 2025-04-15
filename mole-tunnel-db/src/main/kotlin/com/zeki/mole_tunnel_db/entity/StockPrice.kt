package com.zeki.mole_tunnel_db.entity

import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(
    name = "stock_price",
    indexes =
        [
            Index(
                name = "idx_stock_price_stock_info_code",
                columnList = "stock_info_id,date"
            ),
            Index(name = "idx_stock_price_date", columnList = "date")]
)
class StockPrice
private constructor(
    date: LocalDate,
    close: BigDecimal,
    open: BigDecimal,
    high: BigDecimal,
    low: BigDecimal,
    volume: Long,
    rsi: Float? = null,
    volumeAvg5: BigDecimal? = null,
    volumeAvg20: BigDecimal? = null,
    volumeRatio: BigDecimal? = null,
    priceChangeRate: BigDecimal? = null,
    volatility: BigDecimal? = null
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

    @Column(name = "rsi", nullable = true)
    @Comment("RSI")
    var rsi: Float? = rsi
        protected set

    @Column(name = "volume_avg_5", nullable = true)
    @Comment("5일 평균 거래량")
    var volumeAvg5: BigDecimal? = volumeAvg5
        protected set

    @Column(name = "volume_avg_20", nullable = true)
    @Comment("20일 평균 거래량")
    var volumeAvg20: BigDecimal? = volumeAvg20
        protected set

    @Column(name = "volume_ratio", nullable = true)
    @Comment("거래량 비율 (현재 거래량 / 20일 평균 거래량)")
    var volumeRatio: BigDecimal? = volumeRatio
        protected set

    @Column(name = "price_change_rate", nullable = true)
    @Comment("가격 변동률 (%)")
    var priceChangeRate: BigDecimal? = priceChangeRate
        protected set

    @Column(name = "volatility", nullable = true)
    @Comment("변동성 (고가 - 저가) / 시가")
    var volatility: BigDecimal? = volatility
        protected set

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
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
            val stockPrice =
                StockPrice(
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
        if (this.close.compareTo(close) == 0 &&
            this.open.compareTo(open) == 0 &&
            this.high.compareTo(high) == 0 &&
            this.low.compareTo(low) == 0 &&
            this.volume == volume
        )
            return false

        this.close = close
        this.open = open
        this.high = high
        this.low = low
        this.volume = volume

        return true
    }

    /** RSI 업데이트 함수 */
    fun updateRsi(rsi: Float?): Boolean {
        if (this.rsi == rsi) return false
        this.rsi = rsi
        return true
    }

    /** 볼륨 관련 지표 업데이트 함수 */
    fun updateVolumeIndicators(
        volumeAvg5: BigDecimal,
        volumeAvg20: BigDecimal,
        volumeRatio: BigDecimal,
        priceChangeRate: BigDecimal,
        volatility: BigDecimal
    ): Boolean {
        if ((this.volumeAvg5 != null && this.volumeAvg5!!.compareTo(volumeAvg5) == 0) &&
            (this.volumeAvg20 != null && this.volumeAvg20!!.compareTo(volumeAvg20) == 0) &&
            (this.volumeRatio != null && this.volumeRatio!!.compareTo(volumeRatio) == 0) &&
            (this.priceChangeRate != null && this.priceChangeRate!!.compareTo(priceChangeRate) == 0) &&
            (this.volatility != null && this.volatility!!.compareTo(volatility) == 0)
        )
            return false

        this.volumeAvg5 = volumeAvg5
        this.volumeAvg20 = volumeAvg20
        this.volumeRatio = volumeRatio
        this.priceChangeRate = priceChangeRate
        this.volatility = volatility

        return true
    }
}
