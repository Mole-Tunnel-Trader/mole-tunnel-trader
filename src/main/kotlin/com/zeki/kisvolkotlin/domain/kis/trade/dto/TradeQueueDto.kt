package com.zeki.kisvolkotlin.domain.kis.trade.dto

import com.zeki.kisvolkotlin.db.entity.em.OrderType
import java.math.BigDecimal

data class TradeQueueDto(
    val orderBy: String = "",
    val items: List<Item> = listOf(),
) {
    data class Item(
        val id: Long,
        val stockCode: String,
        val orderType: OrderType,
        val orderPrice: BigDecimal,
        val orderAmount: Double,
    )
}