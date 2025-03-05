package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.AlgorithmLogStock
import org.springframework.data.jpa.repository.JpaRepository

interface AlgorithmLogStockRepository : JpaRepository<AlgorithmLogStock, Long> {
}