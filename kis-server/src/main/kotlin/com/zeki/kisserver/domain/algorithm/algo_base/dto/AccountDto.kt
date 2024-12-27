package com.zeki.kisserver.domain.algorithm.algo_base.dto

import com.zeki.common.em.OrderType
import java.math.BigDecimal
import java.time.LocalDate

data class AccountDto(
    var totalPrice: BigDecimal = BigDecimal.ZERO,
    var stockPrice: BigDecimal = BigDecimal.ZERO,
    var cashPrice: BigDecimal = BigDecimal.ZERO,
    var stockInfos: MutableMap<String, StockInfoDto> = mutableMapOf()
) {
    data class StockInfoDto(
        var stockCode: String,
        var stockName: String,
        var stockAvgPrice: BigDecimal,
        var stockAmount: BigDecimal,
        var stockTradeInfos: MutableList<StockTradeInfoDto> = mutableListOf()
    )

    data class StockTradeInfoDto(
        var orderType: OrderType,
        var tradeDate: LocalDate,
        var tradePrice: BigDecimal,
        var tradeAmount: BigDecimal,
    )

}

