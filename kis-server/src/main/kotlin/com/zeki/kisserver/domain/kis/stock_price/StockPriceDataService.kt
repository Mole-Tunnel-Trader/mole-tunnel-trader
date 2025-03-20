package com.zeki.kisserver.domain.kis.stock_price

import com.zeki.mole_tunnel_db.entity.StockPrice
import com.zeki.mole_tunnel_db.repository.StockInfoRepository
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class StockPriceDataService(
    private val stockInfoRepository: StockInfoRepository,
    private val stockPriceRepository: StockPriceRepository,
    private val crawlNaverFinanceService: CrawlNaverFinanceService
) {
    @Transactional(readOnly = true)
    fun prepareStockPriceData(
        stockCodeList: List<String>,
        standardDate: LocalDate,
        count: Int
    ): Pair<List<StockPrice>, List<StockPrice>> {
        val stockPriceSaveList = mutableListOf<StockPrice>()
        val stockPriceUpdateList = mutableListOf<StockPrice>()

        // 1. StockInfo 정보 로드
        val stockInfoList = stockInfoRepository.findByCodeIn(stockCodeList)
        val stockInfoMap = stockInfoList.associateBy { it.code }

        // 2. 모든 종목의 최근 30일 데이터를 한 번에 조회
        val startDate = standardDate.minusDays(20)
        val recentStockPrices =
            stockPriceRepository.findAllByDateBetweenAndStockInfoCodeIn(
                startDate = startDate,
                endDate = standardDate,
                stockCodeList = stockCodeList
            )

        // 3. 종목코드별로 StockPrice 그룹화
        val stockPricesByCode = recentStockPrices.groupBy { it.stockInfo.code }

        // 4. 각 종목별 처리
        for (stockCode in stockCodeList) {
            val stockInfo = stockInfoMap[stockCode] ?: continue

            // 해당 종목의 기존 주가 데이터를 맵으로 변환
            val stockPriceMap =
                (stockPricesByCode[stockCode] ?: emptyList())
                    .associateBy { it.date }
                    .toMutableMap()

            // 네이버 금융에서 데이터 크롤링
            val crawlStockPriceDto =
                crawlNaverFinanceService.crawlStockPrice(stockCode, standardDate, count)

            // 크롤링한 데이터 처리
            crawlStockPriceDto.items.forEach {
                when (val stockPrice = stockPriceMap[it.date]) {
                    null -> {
                        stockPriceSaveList.add(
                            StockPrice.create(
                                date = it.date,
                                open = it.open,
                                high = it.high,
                                low = it.low,
                                close = it.close,
                                volume = it.volume,
                                stockInfo = stockInfo,
                            )
                        )
                    }

                    else -> {
                        val isUpdate =
                            stockPrice.updateStockPrice(
                                open = it.open,
                                high = it.high,
                                low = it.low,
                                close = it.close,
                                volume = it.volume,
                            )
                        if (isUpdate) stockPriceUpdateList.add(stockPrice)
                        stockPriceMap.remove(it.date)
                    }
                }
            }
        }

        return Pair(stockPriceSaveList, stockPriceUpdateList)
    }


}
