package com.zeki.kisvolkotlin.domain.kis.trade

import com.zeki.kisvolkotlin.db.repository.TradeHistoryRepository
import org.springframework.stereotype.Service

@Service
class TradeHistoryService(
    private val tradeHistoryRepository: TradeHistoryRepository
) {
}