package com.zeki.kisvolkotlin.db.repository

import com.zeki.kisvolkotlin.db.entity.Token
import com.zeki.kisvolkotlin.db.entity.em.TradeMode
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository : JpaRepository<Token, Long> {
    fun findFirstByTradeModeOrderByExpiredDateDesc(tradeMode: TradeMode): Token?
}