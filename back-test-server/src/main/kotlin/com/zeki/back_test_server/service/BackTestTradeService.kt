package com.zeki.back_test_server.service

import com.zeki.algorithm.dto.MoleAlgorithmResult
import com.zeki.back_test_server.dto.BackTestAsset
import com.zeki.common.em.OrderType
import com.zeki.mole_tunnel_db.entity.AlgorithmLog
import com.zeki.mole_tunnel_db.entity.AlgorithmLogDate
import com.zeki.mole_tunnel_db.entity.AlgorithmLogDateDetail
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BackTestTradeService(
    private val stockPriceRepository: StockPriceRepository
) {

    /**
     * 1일 기준 매매 함수.
     */
    fun trade(
        nextDay: LocalDate,
        stockCodeList: List<String>,
        backTestAsset: BackTestAsset,
        algorithmLog: AlgorithmLog,
        algoTradeList: List<MoleAlgorithmResult>
    ): List<AlgorithmLogDateDetail> {
        // 전일대비 등락을 비교하기 위해 저장
        val preDepositPrice = backTestAsset.depositPrice
        val preValuationPrice = backTestAsset.valuationPrice

        // 해당일자의 stockPrice 조회
        val stockPriceMap = stockPriceRepository.findAllByStockCodeInAndDate(stockCodeList, nextDay)
            .associateBy { it.stockInfo.code }

        // AlgorithmLogDate 생성
        val algorithmLogDate = AlgorithmLogDate.create(
            algorithmLog = algorithmLog,
            date = nextDay,
            depositPrice = BigDecimal.ZERO,
            valuationPrice = BigDecimal.ZERO,
            beforeAssetRate = 0f,
            totalAssetRate = 0f,
        )

        // AlgorithmLogDateDetail List 생성
        val algorithmLogDateDetailList = mutableListOf<AlgorithmLogDateDetail>()

        for (algoTrade in algoTradeList) {
            val stockCode = algoTrade.stockCode

            // 해당 종목의 주가가 존재하지 않으면 continue
            val stockPriceEntity = stockPriceMap.get(stockCode) ?: continue

            // 해당 종목의 거래량이 존재하지 않으면 continue
            if (stockPriceEntity.volume == 0L) continue

            /* 매매 타입에 따라 분기 */
            if (algoTrade.orderType == OrderType.BUY) {
                /* 매수시 */
                // 해당 날짜에 low ~ hgih 범위에 매매가 존재하지 않으면 continue
                if (algoTrade.tradeStandardPrice < stockPriceEntity.low
                    || algoTrade.tradeStandardPrice > stockPriceEntity.high
                ) continue

                // 매수 금액과 예수금 비교 후 초과시 continue
                if (backTestAsset.depositPrice < algoTrade.tradeStandardPrice * algoTrade.quantity) continue

            } else {
                /* 매도시 */
                // 보유 종목에 해당 종목이 존재하지 않으면 continue
                val stockAsset = backTestAsset.stockMap[stockCode] ?: continue

                // 이전 총 매수금액
                val preBuyTotalPrice = stockAsset.tradeTotalPrice

                // TODO : 수수료 계산해야함
                // 해당 날짜의 시가에 매도
                val sellTotalPrice = stockPriceEntity.open * stockAsset.quantity

                backTestAsset.depositPrice += sellTotalPrice
                // 평가금은 한번에 계산


                // FIXME : 이렇게 저정하면.. 매매 단건 당 수익률과 성공률 계산이 안되는데..
                algorithmLogDateDetailList.add(
                    AlgorithmLogDateDetail.create(
                        algorithmLogDate = algorithmLogDate,
                        stockCode = stockCode,
                        orderType = OrderType.SELL,
                        tradePrice = stockPriceEntity.open,
                        totalTradePrice = sellTotalPrice
                    )
                )
            }

        }

        // TODO : 매매 로직 구현

        return emptyList()
    }

}