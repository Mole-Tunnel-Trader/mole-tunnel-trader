package com.zeki.back_test_server.service

import com.zeki.algorithm.dto.AccountAsset
import com.zeki.algorithm.dto.MoleAlgorithmResult
import com.zeki.common.em.OrderType
import com.zeki.mole_tunnel_db.entity.AlgorithmLog
import com.zeki.mole_tunnel_db.entity.AlgorithmLogDate
import com.zeki.mole_tunnel_db.entity.AlgorithmLogStock
import com.zeki.mole_tunnel_db.entity.StockPrice
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BackTestTradeService() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /** trade함수 결과 DTO */
    data class TradeResult(
        val algorithmLogDate: AlgorithmLogDate,
        val algorithmLogStockList: List<AlgorithmLogStock>
    )

    /** 1일 기준 매매 함수. */
    fun trade(
        nextDay: LocalDate,
        backTestAsset: AccountAsset,
        algorithmLog: AlgorithmLog,
        algoTradeList: List<MoleAlgorithmResult>,
        originDeposit: BigDecimal,
        stockPriceMap: Map<String, StockPrice>
    ): TradeResult {
        // 전일대비 등락을 비교하기 위해 저장
        val preDepositPrice = backTestAsset.depositPrice
        val preValuationPrice = backTestAsset.valuationPrice

        // AlgorithmLogDateDetail List 생성
        val algorithmLogStockList = mutableListOf<AlgorithmLogStock>()

        // 알고리즘의 매매 내역 순회
        for (algoTrade in algoTradeList) {
            val stockCode = algoTrade.stockCode

            // 해당 종목의 주가가 존재하지 않으면 continue
            val stockPriceEntity = stockPriceMap[stockCode] ?: continue

            // 해당 종목의 거래량이 존재하지 않으면 continue
            if (stockPriceEntity.volume == 0L) continue

            /* 매매 타입에 따라 분기 */
            if (algoTrade.orderType == OrderType.BUY) {
                /* 매수시 */
                var buyStandardPrice: BigDecimal
                if (algoTrade.tradeStandardPrice == BigDecimal.ZERO) {
                    // 기준금액이 0원으로 온다면 반드시 시가에 매수
                    buyStandardPrice = stockPriceEntity.open
                } else {
                    // 해당 날짜에 low ~ high 범위에 매매가 존재하지 않으면 continue
                    if (algoTrade.tradeStandardPrice < stockPriceEntity.low ||
                        algoTrade.tradeStandardPrice > stockPriceEntity.high
                    )
                        continue
                    else buyStandardPrice = algoTrade.tradeStandardPrice
                }

                // 매수 총액 계산
                val buyTotalPrice = buyStandardPrice * algoTrade.quantity

                // 매수 금액과 예수금 비교 후 초과시 continue
                if (backTestAsset.depositPrice < buyTotalPrice) {
                    logger.warn(
                        "예수금 부족으로 매수 불가: 종목=${stockCode}, 필요금액=${buyTotalPrice}, 보유예수금=${backTestAsset.depositPrice}"
                    )
                    continue
                }

                // 해당 종목 매수
                backTestAsset.depositPrice = backTestAsset.depositPrice.subtract(buyTotalPrice)

                // 이미 보유 중인 종목이면 수량만 추가
                val existingStockAsset = backTestAsset.stockMap[stockCode]
                if (existingStockAsset != null) {
                    // 기존 종목에 수량 추가
                    logger.info(
                        "기존 보유 종목 추가 매수: ${stockCode}, 기존 수량=${existingStockAsset.quantity}, 추가=${algoTrade.quantity}"
                    )

                    val newQuantity = existingStockAsset.quantity.add(algoTrade.quantity)

                    existingStockAsset.quantity = newQuantity
                    // 현재가는 그대로 유지하고 총액만 업데이트
                    existingStockAsset.currentTotalPrice =
                        existingStockAsset.currentStandardPrice.multiply(newQuantity)
                } else {
                    // 새 종목 추가
                    logger.info(
                        "신규 종목 매수: ${stockCode}, 수량=${algoTrade.quantity}, 금액=${buyTotalPrice}"
                    )
                    backTestAsset.stockMap[stockCode] =
                        AccountAsset.StockAsset(
                            stockCode = stockCode,
                            tradeStandardPrice = buyStandardPrice,
                            quantity = algoTrade.quantity,
                            tradeTotalPrice = buyTotalPrice,
                            currentStandardPrice = buyStandardPrice,
                            currentTotalPrice = buyTotalPrice,
                            holdingDays = 0
                        )
                }

                algorithmLogStockList.add(
                    AlgorithmLogStock.create(
                        algorithmLog = algorithmLog,
                        date = nextDay,
                        stockCode = stockCode,
                        orderType = OrderType.BUY,
                        tradeStandardPrice = buyStandardPrice,
                        quantity = algoTrade.quantity
                    )
                )
            } else {
                /* 매도시 */
                // 보유 종목에 해당 종목이 존재하지 않으면 continue
                val stockAsset = backTestAsset.stockMap[stockCode]
                if (stockAsset == null) {
                    logger.warn("보유하지 않은 종목 매도 시도: ${stockCode}")
                    continue
                }

                // 매도 수량이 보유 수량보다 많으면 보유 수량으로 제한
                val sellQuantity =
                    if (algoTrade.quantity > stockAsset.quantity) {
                        logger.warn(
                            "매도 수량 조정: ${stockCode}, 요청=${algoTrade.quantity}, 보유=${stockAsset.quantity}"
                        )
                        stockAsset.quantity
                    } else {
                        algoTrade.quantity
                    }

                // 매도 가격 결정
                var sellStandardPrice: BigDecimal
                if (algoTrade.tradeStandardPrice == BigDecimal.ZERO) {
                    // 기준금액이 0원으로 온다면 반드시 시가에 매도
                    sellStandardPrice = stockPriceEntity.open
                } else {
                    // 기준금액이 존재한다면 반드시 기준금액에 매도
                    sellStandardPrice = algoTrade.tradeStandardPrice
                }

                // 매도 총금액
                val sellTotalPrice = sellStandardPrice.multiply(sellQuantity)
                logger.info("종목 매도: ${stockCode}, 수량=${sellQuantity}, 금액=${sellTotalPrice}")

                // 잔량 계산
                val remainQuantity = stockAsset.quantity.subtract(sellQuantity)
                if (remainQuantity <= BigDecimal.ZERO) {
                    // 전량 매도
                    backTestAsset.stockMap.remove(stockCode)
                    logger.info("전량 매도 완료: ${stockCode}")
                } else {
                    // 일부 매도 - 수량만 감소하고 보유주식은 유지
                    stockAsset.quantity = remainQuantity
                    // 평가총액도 재계산
                    stockAsset.currentTotalPrice =
                        stockAsset.currentStandardPrice.multiply(remainQuantity)
                    logger.info("일부 매도 완료: ${stockCode}, 잔여수량=${remainQuantity}")
                }

                // 예수금 증가
                backTestAsset.depositPrice = backTestAsset.depositPrice.add(sellTotalPrice)

                algorithmLogStockList.add(
                    AlgorithmLogStock.create(
                        algorithmLog = algorithmLog,
                        date = nextDay,
                        stockCode = stockCode,
                        orderType = OrderType.SELL,
                        tradeStandardPrice = sellStandardPrice,
                        quantity = sellQuantity,
                    )
                )
            }
        }

        // 평가금액 계산 (모든 보유 종목의 현재가 * 수량)
        backTestAsset.valuationPrice =
            backTestAsset.stockMap.values.sumOf {
                it.currentStandardPrice.multiply(it.quantity)
            }

        // AlgorithmLogDate 생성, 날짜별 로그
        val preTotalPrice = preDepositPrice.add(preValuationPrice)
        val nowTotalPrice = backTestAsset.depositPrice.add(backTestAsset.valuationPrice)

        // 수익률 계산 시 0으로 나누는 상황 방지
        val beforeAssetRate =
            if (nowTotalPrice.compareTo(BigDecimal.ZERO) != 0) {
                (preTotalPrice.divide(nowTotalPrice, 4, java.math.RoundingMode.HALF_UP))
                    .toFloat()
            } else {
                1.0f
            }

        val totalAssetRate =
            if (originDeposit.compareTo(BigDecimal.ZERO) != 0) {
                (nowTotalPrice.divide(originDeposit, 4, java.math.RoundingMode.HALF_UP))
                    .toFloat()
            } else {
                1.0f
            }

        logger.info(
            "일일 거래 처리 완료: 날짜=${nextDay}, 예수금=${backTestAsset.depositPrice}, 평가금액=${backTestAsset.valuationPrice}, 총자산=${nowTotalPrice}"
        )

        val algorithmLogDate =
            AlgorithmLogDate.create(
                algorithmLog = algorithmLog,
                date = nextDay,
                depositPrice = backTestAsset.depositPrice,
                valuationPrice = backTestAsset.valuationPrice,
                beforeAssetRate = beforeAssetRate,
                totalAssetRate = totalAssetRate,
            )

        return TradeResult(
            algorithmLogDate = algorithmLogDate,
            algorithmLogStockList = algorithmLogStockList
        )
    }
}
