package com.zeki.kisserver.domain.kis.trade

import com.zeki.kisserver.db.repository.TradeQueueRepository
import com.zeki.kisserver.domain.kis.trade.dto.TradeQueueDto
import org.springframework.stereotype.Service

@Service
class TradeQueueService(
    private val tradeQueueRepository: TradeQueueRepository
) {
    fun getTradeQueue(): List<TradeQueueDto> {

        return listOf()
    }

    fun removeTradeQueue(tradeQueueIdList: List<Long>) {
        tradeQueueRepository.deleteAllByIdInBatch(tradeQueueIdList)
    }
}