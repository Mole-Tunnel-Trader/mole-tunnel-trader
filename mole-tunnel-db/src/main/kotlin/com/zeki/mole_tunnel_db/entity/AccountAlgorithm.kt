package com.zeki.mole_tunnel_db.entity

import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "account_algorithm")
class AccountAlgorithm private constructor(
    account: Account,
    algorithm: Algorithm,
    tradePriceRate: Float
) : BaseEntity() {
    @JoinColumn(name = "account_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var account: Account = account

    @JoinColumn(name = "algorithm_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var algorithm: Algorithm = algorithm

    @Column(name = "trade_price_rate", nullable = false)
    var tradePriceRate: Float = tradePriceRate
        protected set

    companion object {
        fun create(
            account: Account,
            algorithm: Algorithm,
            tradePriceRate: Float
        ): AccountAlgorithm {
            return AccountAlgorithm(account, algorithm, tradePriceRate)
        }
    }
}