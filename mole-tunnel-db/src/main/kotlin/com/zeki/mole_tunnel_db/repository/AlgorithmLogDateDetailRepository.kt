package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.AlgorithmLogDateDetail
import org.springframework.data.jpa.repository.JpaRepository

interface AlgorithmLogDateDetailRepository : JpaRepository<AlgorithmLogDateDetail, Long> {
}