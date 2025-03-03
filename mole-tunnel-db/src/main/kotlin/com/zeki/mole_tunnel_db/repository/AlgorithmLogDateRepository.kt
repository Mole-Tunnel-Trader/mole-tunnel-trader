package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.AlgorithmLogDate
import org.springframework.data.jpa.repository.JpaRepository

interface AlgorithmLogDateRepository : JpaRepository<AlgorithmLogDate, Long> {
}