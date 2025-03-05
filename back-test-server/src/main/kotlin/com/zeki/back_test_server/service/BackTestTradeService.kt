package com.zeki.back_test_server.service

import com.zeki.algorithm.dto.MoleAlgorithmResult
import com.zeki.back_test_server.dto.BackTestAsset
import com.zeki.common.em.OrderType
import com.zeki.mole_tunnel_db.entity.AlgorithmLog
import com.zeki.mole_tunnel_db.entity.AlgorithmLogDate
import com.zeki.mole_tunnel_db.entity.AlgorithmLogStock
import com.zeki.mole_tunnel_db.entity.StockPrice
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BackTestTradeService(
) {

    /**
     * trade함수 결과 DTO
     */
    data class TradeResult(
        val algorithmLogDate: AlgorithmLogDate,
        val algorithmLogStockList: List<AlgorithmLogStock>
    )

    /**
     * 1일 기준 매매 함수.
     */
    fun trade(
        nextDay: LocalDate,
        stockCodeList: List<String>,
        backTestAsset: BackTestAsset,
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
            val stockPriceEntity = stockPriceMap.get(stockCode) ?: continue

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
                    // 해당 날짜에 low ~ hgih 범위에 매매가 존재하지 않으면 continue
                    if (algoTrade.tradeStandardPrice < stockPriceEntity.low
                        || algoTrade.tradeStandardPrice > stockPriceEntity.high
                    ) continue
                    else buyStandardPrice = algoTrade.tradeStandardPrice
                }

                // 매수 금액과 예수금 비교 후 초과시 continue
                if (backTestAsset.depositPrice < algoTrade.tradeStandardPrice * algoTrade.quantity) continue

                // 해당 종목 매수
                backTestAsset.depositPrice -= algoTrade.tradeStandardPrice * algoTrade.quantity
                backTestAsset.valuationPrice += algoTrade.tradeStandardPrice * algoTrade.quantity
                backTestAsset.stockMap[stockCode] =
                    BackTestAsset.StockAsset(
                        stockCode = stockCode,
                        tradeStandardPrice = buyStandardPrice,
                        quantity = algoTrade.quantity,
                        tradeTotalPrice = algoTrade.tradeStandardPrice * algoTrade.quantity,
                        currentStandardPrice = buyStandardPrice,
                        currentTotalPrice = algoTrade.tradeStandardPrice * algoTrade.quantity,
                        holdingDays = 0
                    )
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
                val stockAsset = backTestAsset.stockMap[stockCode] ?: continue

                // TODO : 수수료 계산해야함
                // 해당 날짜의 시가에 매도
                var sellStandardPrice: BigDecimal
                if (algoTrade.tradeStandardPrice == BigDecimal.ZERO) {
                    // 기준금액이 0원으로 온다면 반드시 시가에 매도
                    sellStandardPrice = stockPriceEntity.open
                } else {
                    // 기준금액이 존재한다면 반드시 기준금액에 매도
                    sellStandardPrice = algoTrade.tradeStandardPrice
                }
                val sellTotalPrice = sellStandardPrice * algoTrade.quantity

                // 잔량 계산
                val remainQuantity = stockAsset.quantity - algoTrade.quantity
                if (remainQuantity < BigDecimal.ZERO) {
                    backTestAsset.stockMap.remove(stockCode)
                } else {
                    stockAsset.quantity = remainQuantity
                }

                backTestAsset.depositPrice += sellTotalPrice
                backTestAsset.valuationPrice -= sellTotalPrice
                algorithmLogStockList.add(
                    AlgorithmLogStock.create(
                        algorithmLog = algorithmLog,
                        date = nextDay,
                        stockCode = stockCode,
                        orderType = OrderType.SELL,
                        tradeStandardPrice = sellStandardPrice,
                        quantity = algoTrade.quantity,
                    )
                )
            }
        }

        // AlgorithmLogDate 생성, 날짜별 로그
        val preTotalPrice = preDepositPrice + preValuationPrice
        val nowTotalPrice = backTestAsset.depositPrice + backTestAsset.valuationPrice
        val algorithmLogDate = AlgorithmLogDate.create(
            algorithmLog = algorithmLog,
            date = nextDay,
            depositPrice = backTestAsset.depositPrice,
            valuationPrice = backTestAsset.valuationPrice,
            beforeAssetRate = (preTotalPrice / nowTotalPrice).toFloat(),
            totalAssetRate = (nowTotalPrice / originDeposit).toFloat(),
        )

        return TradeResult(
            algorithmLogDate = algorithmLogDate,
            algorithmLogStockList = algorithmLogStockList
        )
    }

}