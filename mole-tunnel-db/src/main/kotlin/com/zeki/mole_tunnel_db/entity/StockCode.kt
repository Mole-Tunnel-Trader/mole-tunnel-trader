package com.zeki.mole_tunnel_db.entity

import com.zeki.common.em.Status
import com.zeki.common.em.StockMarket
import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "stock_code", indexes = [
        Index(name = "idx_stock_code_code", columnList = "code"),
        Index(name = "idx_stock_code_name", columnList = "name"),
    ]
)
class StockCode(
    code: String,
    name: String,
    market: StockMarket,
    isAlive: Status
) : BaseEntity() {

    @Column(name = "code", nullable = false, length = 20)
    var code: String = code
        protected set

    @Column(name = "name", nullable = false, length = 50)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "market", nullable = false, length = 10)
    var market: StockMarket = market
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "isAlive", nullable = false, length = 10)
    var isAlive: Status = isAlive
        protected set

    fun updateStockCode(
        name: String,
        market: StockMarket,
        isAlive: Status
    ): Boolean {
        if (this.name == name &&
            this.market == market &&
            this.isAlive == isAlive
        ) return false

        this.name = name
        this.market = market
        this.isAlive = isAlive;
        return true
    }

    fun updateIsAlive(isAlive: Status) {
        this.isAlive = isAlive;
    }
}