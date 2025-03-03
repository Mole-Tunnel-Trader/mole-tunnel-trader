package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.AccountAlgorithm
import org.springframework.data.jpa.repository.JpaRepository

interface AccountAlgorithmRepository : JpaRepository<AccountAlgorithm, Long> {
}