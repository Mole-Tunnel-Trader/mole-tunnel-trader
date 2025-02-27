package com.zeki.mole_tunnel_db.entity

import com.zeki.common.em.Status
import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "algorithm")
class Algorithm private constructor(
    status: Status,
    name: String
) : BaseEntity() {
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: Status = status

    @Column(name = "name", nullable = false, length = 50)
    var name: String = name

    @OneToMany(
        mappedBy = "algorithm",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.MERGE, CascadeType.PERSIST]
    )
    var accountAlgorithm: List<AccountAlgorithm> = mutableListOf()

    @OneToMany(
        mappedBy = "algorithm",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.MERGE, CascadeType.PERSIST]
    )
    var algorithmLog: List<AlgorithmLog> = mutableListOf()

    companion object {
        fun create(status: Status, name: String) = Algorithm(
            status = status,
            name = name
        )
    }

    fun addAccountAlgorithm(accountAlgorithm: AccountAlgorithm) {
        this.accountAlgorithm += accountAlgorithm
        accountAlgorithm.algorithm = this
    }

    fun addAlgorithmLog(algorithmLog: AlgorithmLog) {
        this.algorithmLog += algorithmLog
        algorithmLog.algorithm = this
    }

}