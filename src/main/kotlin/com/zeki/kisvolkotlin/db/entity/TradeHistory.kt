package com.zeki.kisvolkotlin.db.entity

import com.zeki.kisvolkotlin.db.entity.em.OrderState
import com.zeki.kisvolkotlin.db.entity.em.OrderType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(
    name = "trade_history", indexes = [
        Index(name = "idx_trade_history_stock_code", columnList = "stock_code"),
        Index(name = "idx_trade_history_order_date", columnList = "order_date"),
        Index(name = "idx_trade_history_order_state", columnList = "order_state"),
    ]
)
class TradeHistory(
    stockCode: String,
    orderDate: LocalDate,
    orderState: OrderState,
    kisOrderNum: String,
    krxOrderNum: String,
    kisOrderTime: String,
    orderBy: String,
    orderType: OrderType,
    orderPrice: BigDecimal,
    orderAmount: Double,
    resultCode: String,
    messageCode: String,
    message: String
) : BaseEntity() {
    @Column(name = "stock_code", nullable = false, length = 20)
    var stockCode: String = stockCode
        protected set

    @Column(name = "order_date", nullable = false)
    var orderDate: LocalDate = orderDate
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false)
    var orderState: OrderState = orderState
        protected set

    @Column(name = "kis_order_num", nullable = false, length = 50)
    var kisOrderNum: String = kisOrderNum
        protected set

    @Column(name = "krx_order_num", nullable = false, length = 50)
    var krxOrderNum: String = krxOrderNum
        protected set

    @Column(name = "kis_order_time", nullable = false, length = 50)
    var kisOrderTime: String = kisOrderTime
        protected set

    @Column(name = "order_by", nullable = false, length = 20)
    var orderBy: String = orderBy
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

    @Column(name = "result_code", nullable = false, length = 20)
    var resultCode: String = resultCode
        protected set

    @Column(name = "message_code", nullable = false, length = 20)
    var messageCode: String = messageCode
        protected set

    @Column(name = "message", nullable = false)
    var message: String = message
        protected set
}