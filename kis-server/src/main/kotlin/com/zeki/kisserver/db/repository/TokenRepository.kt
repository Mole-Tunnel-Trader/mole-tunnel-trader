package com.zeki.kisserver.db.repository

import com.zeki.common.em.TradeMode
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository : JpaRepository<com.zeki.kisserver.db.entity.Token, Long> {
    fun findFirstByTradeModeOrderByExpiredDateDesc(tradeMode: TradeMode): com.zeki.kisserver.db.entity.Token?
}