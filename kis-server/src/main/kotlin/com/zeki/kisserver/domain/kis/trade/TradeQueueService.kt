package com.zeki.kisserver.domain.kis.trade

import com.zeki.mole_tunnel_db.dto.TradeQueueDto
import com.zeki.mole_tunnel_db.repository.TradeQueueRepository
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