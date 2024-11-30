package com.zeki.kisserver.domain.algorithm.algo_base.dto

import java.math.BigDecimal
import java.time.LocalDate

data class AlgoStockSellDto(
    val stockCode: String,
    val stockName: String,
    val sellTargetPrice: BigDecimal = BigDecimal.ZERO,
    val sellTargetAmount: BigDecimal = BigDecimal.ZERO,
    val sellPercent: Float = 0f,
    val detectedDate: LocalDate,
    val currentPrice: BigDecimal = BigDecimal.ZERO,
)
