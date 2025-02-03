package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.StockCode
import org.springframework.data.jpa.repository.JpaRepository

interface StockCodeRepository : JpaRepository<StockCode, Long> {
    fun findByCode(code: String): StockCode?
}