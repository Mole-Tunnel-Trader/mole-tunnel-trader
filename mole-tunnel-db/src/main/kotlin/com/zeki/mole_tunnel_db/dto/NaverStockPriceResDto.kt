package com.zeki.mole_tunnel_db.dto

import java.math.BigDecimal
import java.time.LocalDate

data class NaverStockPriceResDto(
    val stockCode: String,
    val items: List<Item>
) {
    data class Item(
        val date: LocalDate,
        val close: BigDecimal,
        val open: BigDecimal,
        val high: BigDecimal,
        val low: BigDecimal,
        val volume: Long
    )
}