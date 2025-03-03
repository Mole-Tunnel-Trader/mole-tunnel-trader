package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.AlgorithmLog
import org.springframework.data.jpa.repository.JpaRepository

interface AlgorithmLogRepository : JpaRepository<AlgorithmLog, Long> {
}