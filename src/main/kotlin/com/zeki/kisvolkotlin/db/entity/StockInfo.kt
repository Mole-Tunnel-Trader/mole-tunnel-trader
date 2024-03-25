package com.zeki.kisvolkotlin.db.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "stock_info",
    indexes = [
        Index(name = "idx_stock_info_code", columnList = "code", unique = true),
    ]
)
class StockInfo(
    name: String,
    code: String,
    otherCode: String,
    fcamt: Int,
    amount: Long,
    marketCapital: Long,
    capital: Long,
    per: Double,
    pbr: Double,
    eps: Double
) : BaseEntity() {

    @Column(name = "name", nullable = false, length = 50)
    @Comment("종목명")
    var name: String = name
        protected set

    @Column(name = "code", nullable = false, length = 20)
    @Comment("종목 코드")
    var code: String = code
        protected set

    @Column(name = "other_code", nullable = false, length = 20)
    @Comment("종목 단축코드")
    var otherCode: String = otherCode
        protected set

    @Column(name = "fcam", nullable = false)
    @Comment("액면가")
    var fcam: Int = fcamt
        protected set

    @Column(name = "amount", nullable = false)
    @Comment("상장주식수")
    var amount: Long = amount
        protected set

    @Column(name = "market_capital", nullable = false)
    @Comment("시가총액")
    var marketCapital: Long = marketCapital
        protected set

    @Column(name = "capital", nullable = false)
    @Comment("자본금")
    var capital: Long = capital
        protected set

    @Column(name = "per", nullable = false)
    @Comment("PER")
    var per: Double = per
        protected set

    @Column(name = "pbr", nullable = false)
    @Comment("PBR")
    var pbr: Double = pbr
        protected set

    @Column(name = "eps", nullable = false)
    @Comment("EPS")
    var eps: Double = eps
        protected set

    @OneToMany(
        mappedBy = "stockInfo",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE],
        orphanRemoval = true
    )
    @OrderBy("date DESC ")
    val stockPriceList: MutableList<StockPrice> = mutableListOf()

    fun updateStockInfo(
        name: String,
        otherCode: String,
        fcamt: Int,
        amount: Long,
        marketCapital: Long,
        capital: Long,
        per: Double,
        pbr: Double,
        eps: Double
    ): Boolean {
        if (this.name == name &&
            this.otherCode == otherCode &&
            this.fcam == fcamt &&
            this.amount == amount &&
            this.marketCapital == marketCapital &&
            this.capital == capital &&
            this.per == per &&
            this.pbr == pbr &&
            this.eps == eps
        ) return false

        this.name = name
        this.otherCode = otherCode
        this.fcam = fcamt
        this.amount = amount
        this.marketCapital = marketCapital
        this.capital = capital
        this.per = per
        this.pbr = pbr
        this.eps = eps
        return true
    }

    fun addStockPrice(stockPrice: StockPrice) {
        stockPrice.regStockInfo(this)
        stockPriceList.add(stockPrice)
    }

}