package com.zeki.kisserver.domain.algorithm.algo_base.dto

import java.math.BigDecimal
import java.time.LocalDate

class AlgoStockBuyDto(
    val stockCode: String,
    val stockName: String,
    val buyTargetPrice: BigDecimal = BigDecimal.ZERO,
    val buyTargetAmount: BigDecimal = BigDecimal.ZERO,
    val allocatedPercent: Float = 0f,
    val detectedDate: LocalDate,
    val currentPrice: BigDecimal = BigDecimal.ZERO,
)
