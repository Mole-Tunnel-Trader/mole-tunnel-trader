package com.zeki.mole_tunnel_db.entity

import com.zeki.common.em.OrderType
import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(
    name = "trade_queue", indexes = [
        Index(name = "idx_trade_queue_stock_code", columnList = "stock_code"),
        Index(name = "idx_trade_queue_order_date", columnList = "order_date"),
        Index(name = "idx_trade_queue_order_type", columnList = "order_type"),
        Index(name = "idx_trade_queue_order_by", columnList = "order_by"),
    ]
)
class TradeQueue(
    stockCode: String,
    stockName: String,
    orderDate: LocalDate,
    orderType: OrderType,
    orderPrice: BigDecimal,
    orderAmount: Double,
    orderBy: String
) : BaseEntity() {
    @Column(name = "stock_code", nullable = false, length = 20)
    var stockCode: String = stockCode
        protected set

    @Column(name = "stock_name", nullable = false, length = 50)
    var stockName: String = stockName
        protected set

    @Column(name = "order_date", nullable = false)
    var orderDate: LocalDate = orderDate
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    var orderType: OrderType = orderType
        protected set

    @Column(name = "order_price", nullable = false, precision = 12)
    var orderPrice: BigDecimal = orderPrice
        protected set

    @Column(name = "order_amount", nullable = false)
    var orderAmount: Double = orderAmount
        protected set

    @Column(name = "order_by", nullable = false, length = 20)
    var orderBy: String = orderBy
        protected set
}