package com.zeki.cursor_algorithm

import com.zeki.algorithm.MoleAlgorithm
import com.zeki.algorithm.dto.AccountAsset
import com.zeki.algorithm.dto.MoleAlgorithmResult
import com.zeki.common.em.OrderType
import com.zeki.cursor_algorithm.service.CursorIndicatorService
import com.zeki.stockcode.service.GetStockCodeService
import com.zeki.stockdata.service.stock_price.StockPriceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

/** 볼륨(거래량) 기반 알고리즘 거래량의 급격한 증가와 가격 변동을 분석하여 매매 신호를 생성합니다. */
@Component
class CursorAlgorithm(
    private val cursorIndicatorService: CursorIndicatorService,
    private val getStockCodeService: GetStockCodeService,
    private val stockPriceService: StockPriceService,
) : MoleAlgorithm {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override val id: Long = 2L
    override val name: String = "cursor"

    // 알고리즘 설정값
    private val volumeRatioThreshold = BigDecimal("1.5") // 거래량 비율 임계값 (평균 대비 1.5배로 완화)
    private val volatilityThreshold = BigDecimal("0.03") // 변동성 임계값 (3%로 완화)
    private val maxStocksToBuy = 5 // 한 번에 매수할 최대 종목 수 증가

    /** 알고리즘 실행 함수 백테스트 서버에서 호출되는 함수 */
    override fun runAlgorithm(
        stockCodeList: List<String>,
        standradDate: LocalDate,
        allowPrice: BigDecimal,
        accountAsset: AccountAsset
    ): List<MoleAlgorithmResult> {
        logger.info(
            "CursorAlgorithm 실행: 날짜 = $standradDate, 종목 수 = ${stockCodeList.size}, 계좌 예수금 = ${accountAsset.depositPrice}"
        )

        if (stockCodeList.isEmpty()) {
            logger.info("매매 대상 종목이 없습니다.")
            return emptyList()
        }

        // 볼륨 지표 계산
        try {
            val indicatorCount =
                cursorIndicatorService.calculateAndSaveIndicators(standradDate, stockCodeList)
            logger.info("$standradDate 날짜에 대해 $indicatorCount 개의 지표가 계산되었습니다.")
        } catch (e: Exception) {
            logger.error("지표 계산 중 오류 발생: ${e.message}", e)
            // 오류가 발생해도 이미 계산된 지표를 기반으로 알고리즘 실행
        }

        val resultList = mutableListOf<MoleAlgorithmResult>()

        // 1. 매수 신호 생성 - 거래량 급증 & 가격 상승 종목
        try {
            val buySignals =
                generateBuySignals(standradDate, stockCodeList, allowPrice, accountAsset)
            resultList.addAll(buySignals)
            logger.info("매수 신호 생성 완료: ${buySignals.size}개")
        } catch (e: Exception) {
            logger.error("매수 신호 생성 중 오류 발생: ${e.message}", e)
        }

        // 2. 매도 신호 생성 - 변동성이 높은 종목
        try {
            val sellSignals = generateSellSignals(standradDate, stockCodeList, accountAsset)
            resultList.addAll(sellSignals)
            logger.info("매도 신호 생성 완료: ${sellSignals.size}개")
        } catch (e: Exception) {
            logger.error("매도 신호 생성 중 오류 발생: ${e.message}", e)
        }

        logger.info(
            "알고리즘 실행 결과: ${resultList.size}개 매매 신호 생성 (매수: ${resultList.count { it.orderType == OrderType.BUY }}, 매도: ${resultList.count { it.orderType == OrderType.SELL }})"
        )
        return resultList
    }

    /** 매수 신호 생성 함수 거래량이 급증하고 가격이 상승한 종목을 선정하여 매수 신호 생성 */
    private fun generateBuySignals(
        date: LocalDate,
        stockCodeList: List<String>,
        allowPrice: BigDecimal,
        accountAsset: AccountAsset
    ): List<MoleAlgorithmResult> {
        // 거래량 급증 & 가격 상승 종목 조회
        val volumeSpikes =
            cursorIndicatorService.getVolumeSpikes(
                date = date,
                minVolumeRatio = volumeRatioThreshold,
                priceIncreaseOnly = true
            )

        logger.info("거래량 급증 & 가격 상승 종목 수: ${volumeSpikes.size}개")

        // 매수 대상 종목 필터링
        val buyTargets =
            volumeSpikes
                .filter { stockCodeList.contains(it.stockInfo.code) } // 주어진 종목 리스트에 포함된 종목만
                .sortedByDescending { it.volumeRatio } // 거래량 비율이 높은 순으로 정렬
                .take(maxStocksToBuy) // 상위 N개 종목만 선택

        logger.info("필터링된 매수 대상 종목 수: ${buyTargets.size}개")

        // 종목당 할당 예산 계산
        val budgetPerStock = allowPrice.divide(BigDecimal("5"), 0, java.math.RoundingMode.DOWN)
        logger.info("종목당 할당 예산: $budgetPerStock (전체 예산의 1/5)")

        // 매수 신호 생성
        return buyTargets.mapNotNull { indicator ->
            // 종목의 현재가 조회 (없으면 건너뜀)
            val currentPrice =
                cursorIndicatorService.getLatestPrice(indicator.stockInfo.code, date)
                    ?: run {
                        logger.warn("${indicator.stockInfo.code} 종목의 현재가를 조회할 수 없습니다.")
                        return@mapNotNull null
                    }

            // 매수 가능 수량 계산
            val quantity =
                if (currentPrice > BigDecimal.ZERO) {
                    budgetPerStock.divide(currentPrice, 0, java.math.RoundingMode.DOWN)
                } else {
                    BigDecimal.ONE // 현재가 정보가 없을 경우 최소 수량
                }

            // 매수 수량이 0이면 신호 생성하지 않음
            if (quantity <= BigDecimal.ZERO) {
                logger.warn("${indicator.stockInfo.code} 종목의 매수 가능 수량이 0입니다.")
                return@mapNotNull null
            }

            MoleAlgorithmResult(
                standardDate = date,
                stockCode = indicator.stockInfo.code,
                tradeStandardPrice = BigDecimal.ZERO, // 시장가 매수
                orderType = OrderType.BUY,
                quantity = quantity // 예산 기반 계산 수량
            )
        }
    }

    /** 매도 신호 생성 함수 변동성이 높은 종목을 선정하여 매도 신호 생성 */
    private fun generateSellSignals(
        date: LocalDate,
        stockCodeList: List<String>,
        accountAsset: AccountAsset
    ): List<MoleAlgorithmResult> {
        // 변동성이 높은 종목 조회
        val highVolatilityStocks =
            cursorIndicatorService.getHighVolatilityStocks(
                date = date,
                minVolumeRatio = volumeRatioThreshold,
                minVolatility = volatilityThreshold // 변동성 3% 이상으로 완화
            )

        logger.info("변동성이 높은 종목 수: ${highVolatilityStocks.size}개")

        // 매도 대상 종목 필터링 - 실제 보유 중인 종목만 포함
        val sellTargets =
            highVolatilityStocks
                .filter { stockCodeList.contains(it.stockInfo.code) } // 주어진 종목 리스트에 포함된 종목만
                .filter { accountAsset.stockMap.containsKey(it.stockInfo.code) } // 보유 중인 종목만 포함
                .sortedByDescending { it.volatility } // 변동성이 높은 순으로 정렬
                .take(maxStocksToBuy) // 상위 N개 종목만 선택

        logger.info("필터링된 매도 대상 종목 수: ${sellTargets.size}개")

        // 매도 신호 생성 - 현재 보유 중인 수량의 50%를 매도
        return sellTargets.mapNotNull { indicator ->
            // 현재 보유 수량 조회
            val stockAsset =
                accountAsset.stockMap[indicator.stockInfo.code]
                    ?: run {
                        logger.warn("${indicator.stockInfo.code} 종목의 보유 정보를 조회할 수 없습니다.")
                        return@mapNotNull null
                    }

            // 매도 수량
            val quantity = stockAsset.quantity

            // 매도 수량이 0이면 신호 생성하지 않음
            if (quantity <= BigDecimal.ZERO) {
                logger.warn("${indicator.stockInfo.code} 종목의 매도 가능 수량이 0입니다.")
                return@mapNotNull null
            }

            MoleAlgorithmResult(
                standardDate = date,
                stockCode = indicator.stockInfo.code,
                tradeStandardPrice = BigDecimal.ZERO, // 시장가 매도
                orderType = OrderType.SELL,
                quantity = quantity // 보유량 기반 계산 수량
            )
        }
    }
}
