package com.zeki.mole_tunnel_db.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "algorithm_log_date")
class AlgorithmLogDate(
    algorithmLog: AlgorithmLog,
    date: LocalDate,
    depositPrice: BigDecimal,
    valuationPrice: BigDecimal,
    beforeAssetRate: Float,
    totalAssetRate: Float
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "date", nullable = false)
    var date: LocalDate = date
        protected set

    @Column(name = "deposit_price", nullable = false, precision = 38, scale = 18)
    var depositPrice: BigDecimal = depositPrice
        protected set

    @Column(name = "valuation_price", nullable = false, precision = 38, scale = 18)
    var valuationPrice: BigDecimal = valuationPrice
        protected set

    @Column(name = "before_asset_rate", nullable = false)
    var beforeAssetRate: Float = beforeAssetRate
        protected set

    @Column(name = "total_asset_rate", nullable = false)
    var totalAssetRate: Float = totalAssetRate
        protected set

    @JoinColumn(name = "algorithm_log_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var algorithmLog: AlgorithmLog = algorithmLog

    companion object {
        fun create(
            algorithmLog: AlgorithmLog,
            date: LocalDate,
            depositPrice: BigDecimal,
            valuationPrice: BigDecimal,
            beforeAssetRate: Float,
            totalAssetRate: Float
        ): AlgorithmLogDate {
            return AlgorithmLogDate(
                algorithmLog,
                date,
                depositPrice,
                valuationPrice,
                beforeAssetRate,
                totalAssetRate
            )
        }
    }
}