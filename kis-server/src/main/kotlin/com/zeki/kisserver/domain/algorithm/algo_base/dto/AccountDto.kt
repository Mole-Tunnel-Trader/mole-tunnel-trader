package com.zeki.kisserver.domain.algorithm.algo_base.dto

import java.math.BigDecimal

data class AccountDto(
    var totalPrice: BigDecimal = BigDecimal.ZERO,
    var stockPrice: BigDecimal = BigDecimal.ZERO,
    var cashPrice: BigDecimal = BigDecimal.ZERO,
    var stockInfos: MutableList<StockInfoDto> = emptyList<StockInfoDto>().toMutableList()
) {

    data class StockInfoDto(
        var stockCode: String,
        var stockName: String,
        var stockPrice: BigDecimal,
        var stockAmount: BigDecimal,
        //TODO : 추후에 추가
    )
}

