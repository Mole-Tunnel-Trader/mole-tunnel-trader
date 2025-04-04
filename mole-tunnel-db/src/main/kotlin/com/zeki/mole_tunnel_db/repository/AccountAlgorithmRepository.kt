package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.AccountAlgorithm
import com.zeki.mole_tunnel_db.entity.Algorithm
import org.springframework.data.jpa.repository.JpaRepository

interface AccountAlgorithmRepository : JpaRepository<AccountAlgorithm, Long> {
    // 알고리즘별 계좌 연동 정보 조회
    fun findByAlgorithm(algorithm: Algorithm): List<AccountAlgorithm>
}
