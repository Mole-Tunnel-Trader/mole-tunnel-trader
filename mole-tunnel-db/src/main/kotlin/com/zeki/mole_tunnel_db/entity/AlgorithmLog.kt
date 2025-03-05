package com.zeki.mole_tunnel_db.entity

import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "algorithm_log")
class AlgorithmLog private constructor(
    startDate: LocalDate,
    endDate: LocalDate,
    depositPrice: BigDecimal,
    algorithm: Algorithm
) : BaseEntity() {
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = startDate
        protected set

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate = endDate
        protected set

    @Column(name = "deposit_price", nullable = false, precision = 38, scale = 18)
    var depositPrice: BigDecimal = depositPrice
        protected set

    @JoinColumn(name = "algorithm_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE, CascadeType.PERSIST])
    var algorithm: Algorithm = algorithm

    @OneToMany(
        mappedBy = "algorithmLog",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.MERGE, CascadeType.PERSIST]
    )
    var algorithmLogDateList: List<AlgorithmLogDate> = mutableListOf()

    @OneToMany(
        mappedBy = "algorithmLog",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.MERGE, CascadeType.PERSIST]
    )
    var algorithmLogStockList: List<AlgorithmLogStock> = mutableListOf()

    companion object {
        fun create(
            startDate: LocalDate,
            endDate: LocalDate,
            depositPrice: BigDecimal,
            algorithm: Algorithm
        ): AlgorithmLog {
            return AlgorithmLog(startDate, endDate, depositPrice, algorithm)
        }
    }

    fun addAlgorithmLogDate(algorithmLogDate: AlgorithmLogDate) {
        this.algorithmLogDateList += algorithmLogDate
        algorithmLogDate.algorithmLog = this
    }

    fun addAlgorithmLogStock(algorithmLogStock: AlgorithmLogStock) {
        this.algorithmLogStockList += algorithmLogStock
        algorithmLogStock.algorithmLog = this
    }
}