package com.zeki.token

import com.zeki.common.em.TradeMode
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository : JpaRepository<Token, Long> {
    fun findFirstByTradeModeOrderByExpiredDateDesc(tradeMode: TradeMode): Token?
}