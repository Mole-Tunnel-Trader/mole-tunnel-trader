package com.zeki.algorithm.dto

import com.zeki.common.em.OrderType
import java.math.BigDecimal
import java.time.LocalDate

data class MoleAlgorithmResult(
    private val standardDate: LocalDate, // 매매일자
    private val stockCode: String,  // 종목코드
    private val tradePrice: BigDecimal, // 매매가
    private val orderType: OrderType,   // 매매구분
    private val tradeTotalPrice: BigDecimal,  // 매매총액
)