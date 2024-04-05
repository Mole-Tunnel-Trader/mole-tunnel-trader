package com.zeki.kisserver.domain.kis.trade

import com.zeki.trade.TradeQueueRepository
import com.zeki.trade.dto.TradeQueueDto
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