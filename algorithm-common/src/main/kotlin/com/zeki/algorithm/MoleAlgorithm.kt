package com.zeki.algorithm

import com.zeki.algorithm.dto.MoleAlgorithmResult
import java.math.BigDecimal
import java.time.LocalDate

interface MoleAlgorithm {
    val id: Long
    val name: String

    fun runAlgorithm(
        stockCodeList: List<String>,
        standradDate: LocalDate,
        allowPrice: BigDecimal
    ): List<MoleAlgorithmResult>
}