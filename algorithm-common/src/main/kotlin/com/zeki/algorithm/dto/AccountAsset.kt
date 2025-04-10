package com.zeki.algorithm.dto

import java.math.BigDecimal

data class AccountAsset(
    var depositPrice: BigDecimal, // 예수금
    var valuationPrice: BigDecimal, // 평가금액
    val stockMap: MutableMap<String, StockAsset> = mutableMapOf() // 보유 종목 Map
) {

    data class StockAsset(
        val stockCode: String, // 종목 코드
        val tradeStandardPrice: BigDecimal, // 매수가
        var quantity: BigDecimal, // 보유 수량
        val tradeTotalPrice: BigDecimal, // 매수 총액
        var currentStandardPrice: BigDecimal, // 현재가
        var currentTotalPrice: BigDecimal, // 현재 총액
        var holdingDays: Int // 보유 일 수
    )
}
