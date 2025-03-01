package com.zeki.mole_tunnel_db.entity

import com.zeki.common.em.OrderType
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "algorithm_log_date_detail")
class AlgorithmLogDateDetail(
    algorithmLogDate: AlgorithmLogDate,
    stockCode: String,
    orderType: OrderType,
    tradePrice: BigDecimal,
    totalTradePrice: BigDecimal
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "stock_code", nullable = false, length = 10)
    var stockCode: String = stockCode
        protected set

    @Column(name = "order_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    var orderType: OrderType = orderType
        protected set

    @Column(name = "trade_price", nullable = false, precision = 38, scale = 18)
    var tradePrice: BigDecimal = tradePrice
        protected set

    @Column(name = "total_trade_price", nullable = false, precision = 38, scale = 18)
    var totalTradePrice: BigDecimal = totalTradePrice
        protected set

    @JoinColumn(name = "algorithm_log_date_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var algorithmLogDate: AlgorithmLogDate = algorithmLogDate

    companion object {
        fun create(
            algorithmLogDate: AlgorithmLogDate,
            stockCode: String,
            orderType: OrderType,
            tradePrice: BigDecimal,
            totalTradePrice: BigDecimal
        ): AlgorithmLogDateDetail {
            return AlgorithmLogDateDetail(
                algorithmLogDate,
                stockCode,
                orderType,
                tradePrice,
                totalTradePrice
            ).apply {
                algorithmLogDate.addAlgorithmLogDateDetail(this)
            }
        }
    }
}