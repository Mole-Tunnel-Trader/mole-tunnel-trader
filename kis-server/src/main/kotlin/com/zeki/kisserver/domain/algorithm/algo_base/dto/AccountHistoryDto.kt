package com.zeki.kisserver.domain.algorithm.algo_base.dto

data class AccountHistoryDto(
    var winCount: Int = 0,
    var loseCount: Int = 0,
    var winProfit: Float = 0f,
    var loseProfit: Float = 0f,
)
