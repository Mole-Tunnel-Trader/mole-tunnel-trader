package com.zeki.mole_tunnel_db.repository

import com.zeki.common.em.TradeMode
import com.zeki.mole_tunnel_db.entity.Token
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository : JpaRepository<Token, Long> {
    fun findFirstByTradeModeOrderByExpiredDateDesc(tradeMode: TradeMode): Token?
}