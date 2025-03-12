package com.zeki.back_test_server.service.algo

import com.zeki.algorithm.MoleAlgorithm
import com.zeki.algorithm.dto.MoleAlgorithmResult
import com.zeki.common.em.OrderType
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.stereotype.Component

@Component
class TestAlgo1 : MoleAlgorithm {
    override val id: Long = 1L
    override val name: String = "테스트 알고리즘 1"

    override fun runAlgorithm(
            stockCodeList: List<String>,
            standradDate: LocalDate,
            allowPrice: BigDecimal
    ): List<MoleAlgorithmResult> {
        val results = mutableListOf<MoleAlgorithmResult>()

        // 삼성전자 코드가 있으면 매수 신호 생성
        if (stockCodeList.contains("005930")) {
            // 삼성전자 매수 신호 (예산의 30% 사용)
            val samsungBuyAmount = allowPrice.multiply(BigDecimal("0.3"))
            val samsungQuantity =
                    samsungBuyAmount.divide(BigDecimal("61000"), 0, BigDecimal.ROUND_DOWN)

            if (samsungQuantity > BigDecimal.ZERO) {
                results.add(
                        MoleAlgorithmResult(
                                standardDate = standradDate,
                                stockCode = "005930",
                                tradeStandardPrice = BigDecimal("61000"),
                                orderType = OrderType.BUY,
                                quantity = samsungQuantity
                        )
                )
            }
        }

        // 카카오 코드가 있으면 매수 신호 생성
        if (stockCodeList.contains("035720")) {
            // 카카오 매수 신호 (예산의 20% 사용)
            val kakaoBuyAmount = allowPrice.multiply(BigDecimal("0.2"))
            val kakaoQuantity = kakaoBuyAmount.divide(BigDecimal("51000"), 0, BigDecimal.ROUND_DOWN)

            if (kakaoQuantity > BigDecimal.ZERO) {
                results.add(
                        MoleAlgorithmResult(
                                standardDate = standradDate,
                                stockCode = "035720",
                                tradeStandardPrice = BigDecimal("51000"),
                                orderType = OrderType.BUY,
                                quantity = kakaoQuantity
                        )
                )
            }
        }

        // 다음날에는 네이버 매수 신호 생성 (예산의 10% 사용)
        if (standradDate.isEqual(LocalDate.of(2023, 1, 3)) && stockCodeList.contains("035420")) {
            val naverBuyAmount = allowPrice.multiply(BigDecimal("0.1"))
            val naverQuantity =
                    naverBuyAmount.divide(BigDecimal("305000"), 0, BigDecimal.ROUND_DOWN)

            if (naverQuantity > BigDecimal.ZERO) {
                results.add(
                        MoleAlgorithmResult(
                                standardDate = standradDate,
                                stockCode = "035420",
                                tradeStandardPrice = BigDecimal("305000"),
                                orderType = OrderType.BUY,
                                quantity = naverQuantity
                        )
                )
            }
        }

        // 마지막 날에는 삼성전자 매도 신호 생성
        if (standradDate.isEqual(LocalDate.of(2023, 1, 4)) && stockCodeList.contains("005930")) {
            results.add(
                    MoleAlgorithmResult(
                            standardDate = standradDate,
                            stockCode = "005930",
                            tradeStandardPrice = BigDecimal("61000"),
                            orderType = OrderType.SELL,
                            quantity = BigDecimal("30") // 보유 수량의 일부만 매도
                    )
            )
        }

        return results
    }
}
