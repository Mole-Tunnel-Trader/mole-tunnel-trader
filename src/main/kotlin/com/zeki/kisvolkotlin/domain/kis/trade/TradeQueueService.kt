package com.zeki.kisvolkotlin.domain.kis.trade

import com.zeki.kisvolkotlin.db.repository.TradeQueueRepository
import com.zeki.kisvolkotlin.domain.kis.trade.dto.TradeQueueDto
import org.springframework.stereotype.Service

@Service
class TradeQueueService(
    private val tradeQueueRepository: TradeQueueRepository
) {
    fun getTradeQueue(): List<TradeQueueDto> {

        return listOf()
    }
}