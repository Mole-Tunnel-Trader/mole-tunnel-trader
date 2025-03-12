package com.zeki.back_test_server.service.algo

import com.zeki.algorithm.MoleAlgorithm
import com.zeki.algorithm.dto.MoleAlgorithmResult
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class TestAlgo2 : MoleAlgorithm {
    override val id: Long = 2L
    override val name: String = "테스트 알고리즘 2"

    override fun runAlgorithm(
        stockCodeList: List<String>,
        standradDate: LocalDate,
        allowPrice: BigDecimal
    ): List<MoleAlgorithmResult> {

        return emptyList()
    }

}