package com.zeki.mole_tunnel_db.entity

import com.zeki.common.em.OrderType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "algorithm_log_stock")
class AlgorithmLogStock(
    algorithmLog: AlgorithmLog,
    date: LocalDate,
    stockCode: String,
    orderType: OrderType,
    tradeStandardPrice: BigDecimal,
    quantity: BigDecimal
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "date", nullable = false)
    var date: LocalDate = date
        protected set

    @Column(name = "stock_code", nullable = false, length = 10)
    var stockCode: String = stockCode
        protected set

    @Column(name = "order_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    var orderType: OrderType = orderType
        protected set

    @Column(name = "trade_standard_price", nullable = false, precision = 38, scale = 18)
    var tradeStandardPrice: BigDecimal = tradeStandardPrice
        protected set

    @Column(name = "quantity", nullable = false, precision = 38, scale = 18)
    var quantity: BigDecimal = quantity
        protected set

    @JoinColumn(name = "algorithm_log_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var algorithmLog: AlgorithmLog = algorithmLog

    companion object {
        fun create(
            algorithmLog: AlgorithmLog,
            date: LocalDate,
            stockCode: String,
            orderType: OrderType,
            tradeStandardPrice: BigDecimal,
            quantity: BigDecimal
        ): AlgorithmLogStock {
            return AlgorithmLogStock(
                algorithmLog,
                date,
                stockCode,
                orderType,
                tradeStandardPrice,
                quantity
            ).apply {
                this.algorithmLog.addAlgorithmLogStock(this)
            }
        }
    }
}