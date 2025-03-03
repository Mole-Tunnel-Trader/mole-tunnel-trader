package com.zeki

import com.zeki.algorithm.MoleAlgorithm
import com.zeki.algorithm.dto.MoleAlgorithmResult
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class TestAlgo : MoleAlgorithm {
    override val id: Long = 1L
    override val name: String = "test"

    override fun runAlgorithm(
        stockCodeList: List<String>,
        standradDate: LocalDate,
        allowPrice: BigDecimal
    ): List<MoleAlgorithmResult> {

        return emptyList()
    }

}