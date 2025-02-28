package com.zeki.algorithm.dto

import com.zeki.common.em.OrderType
import java.math.BigDecimal
import java.time.LocalDate

data class MoleAlgorithmResult(
    val standardDate: LocalDate, // 매매일자
    val stockCode: String,  // 종목코드
    val tradeStandardPrice: BigDecimal, // 매매가
    val orderType: OrderType,   // 매매구분
    val quantity: BigDecimal, // 매매수량
)