package com.zeki.mole_tunnel_db.repository

import com.zeki.common.em.Status
import com.zeki.common.em.StockMarket
import com.zeki.mole_tunnel_db.dto.jpa.StockCodeOnly
import com.zeki.mole_tunnel_db.entity.StockCode
import org.springframework.data.jpa.repository.JpaRepository

interface StockCodeRepository : JpaRepository<StockCode, Long> {
    fun findByCode(code: String): StockCode?
    fun findAllByIsAlive(status: Status = Status.Y): MutableList<StockCodeOnly>

    fun findByIsAlive(status: Status = Status.Y): MutableList<StockCode>

    fun findByIsAliveAndMarketIn(isAlive: Status, market: MutableCollection<StockMarket>): MutableList<StockCodeOnly>

    fun findByName(name: String) : MutableList<StockCodeOnly>
}