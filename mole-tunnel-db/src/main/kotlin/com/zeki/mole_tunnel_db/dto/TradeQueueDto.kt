package com.zeki.mole_tunnel_db.dto

import com.zeki.common.em.OrderType
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