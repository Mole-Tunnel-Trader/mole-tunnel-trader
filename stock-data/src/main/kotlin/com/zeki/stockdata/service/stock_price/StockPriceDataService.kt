package com.zeki.stockdata.service.stock_price

import com.zeki.mole_tunnel_db.entity.StockPrice
import com.zeki.mole_tunnel_db.repository.StockInfoRepository
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import com.zeki.stockdata.service.stock_info.CrawlNaverFinanceService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class StockPriceDataService(
    private val stockInfoRepository: StockInfoRepository,
    private val stockPriceRepository: StockPriceRepository,
    private val crawlNaverFinanceService: CrawlNaverFinanceService
) {

    private val log = KotlinLogging.logger {}

    @Transactional(readOnly = true)
    fun prepareStockPriceData(
        stockCodeList: List<String>,
        standardDate: LocalDate,
        count: Int
    ): Pair<List<StockPrice>, List<StockPrice>> {
        val stockPriceSaveList = mutableListOf<StockPrice>()
        val stockPriceUpdateList = mutableListOf<StockPrice>()

        // 디버깅용 지표
        var stockInfoNotFoundCount = 0
        var processedStockCount = 0
        val itemsPerStockCode = mutableMapOf<String, Int>()

        // 1. StockInfo 정보 로드
        val stockInfoList = stockInfoRepository.findByCodeIn(stockCodeList)
        val stockInfoMap = stockInfoList.associateBy { it.code }

        // 2. 모든 종목의 최근 30일 데이터를 한 번에 조회
        val startDate = standardDate.minusDays(count.toLong())
        val recentStockPrices =
            stockPriceRepository.findAllByDateBetweenAndStockInfoCodeIn(
                startDate = startDate,
                endDate = standardDate,
                stockCodeList = stockCodeList
            )

        // 3. 종목코드와 날짜로 StockPrice 그룹화 (빠른 검색을 위한 맵 구조)
        val stockPriceByCodeAndDate = mutableMapOf<String, MutableMap<LocalDate, StockPrice>>()
        recentStockPrices.forEach { stockPrice ->
            val code = stockPrice.stockInfo.code
            stockPriceByCodeAndDate
                .getOrPut(code) { mutableMapOf() }
                .put(stockPrice.date, stockPrice)
        }

        // 4. 각 종목별 처리
        for (stockCode in stockCodeList) {
            val stockInfo = stockInfoMap[stockCode]
            if (stockInfo == null) {
                stockInfoNotFoundCount++
                continue
            }

            processedStockCount++
            val saveCountBefore = stockPriceSaveList.size
            val updateCountBefore = stockPriceUpdateList.size

            // 해당 종목의 주가 데이터 맵
            val pricesByDate = stockPriceByCodeAndDate.getOrPut(stockCode) { mutableMapOf() }

            // 네이버 금융에서 데이터 크롤링
            val crawlStockPriceDto =
                crawlNaverFinanceService.crawlStockPrice(stockCode, standardDate, count)

            // 크롤링한 데이터 처리
            crawlStockPriceDto.items.forEach { item ->
                // 이미 존재하는 데이터인지 확인
                val existingStockPrice = pricesByDate[item.date]

                if (existingStockPrice == null) {
                    // 새로운 데이터라면 신규 생성
                    try {
                        val newStockPrice =
                            StockPrice.create(
                                date = item.date,
                                open = item.open,
                                high = item.high,
                                low = item.low,
                                close = item.close,
                                volume = item.volume,
                                stockInfo = stockInfo
                            )
                        stockPriceSaveList.add(newStockPrice)
                        // 생성된 데이터를 맵에 추가 (후속 처리에서 중복 생성 방지)
                        pricesByDate[item.date] = newStockPrice
                    } catch (e: Exception) {
                        // 이미 다른 스레드에서 동일한 데이터를 생성했을 수 있음, 로그만 남기고 계속 진행
                        println("데이터 생성 중 오류: ${e.message} - 종목: $stockCode, 날짜: ${item.date}")
                    }
                } else {
                    // 기존 데이터가 있다면 값이 변경되었는지 확인 후 업데이트
                    val isUpdate =
                        existingStockPrice.updateStockPrice(
                            open = item.open,
                            high = item.high,
                            low = item.low,
                            close = item.close,
                            volume = item.volume,
                        )
                    // 실제로 변경된 경우에만 업데이트 리스트에 추가
                    if (isUpdate) stockPriceUpdateList.add(existingStockPrice)
                }
            }

            val savedCount = stockPriceSaveList.size - saveCountBefore
            val updatedCount = stockPriceUpdateList.size - updateCountBefore
            itemsPerStockCode[stockCode] = savedCount + updatedCount

            // 일정 수 이상의 레코드가 처리되면 경고 로그 남기기
            if (savedCount + updatedCount > 15) {
                log.warn("주의: 종목 $stockCode 에서 많은 데이터 처리됨 - 저장: $savedCount, 업데이트: $updatedCount")
            }
        }

        // 종합 로그
        println("종목 처리 결과 - 전체: ${stockCodeList.size}, 처리됨: $processedStockCount, StockInfo 없음: $stockInfoNotFoundCount")
        println("저장: ${stockPriceSaveList.size}, 업데이트: ${stockPriceUpdateList.size}")

        // 비정상적으로 많은 데이터를 처리한 종목 로깅
        val suspiciousStocks = itemsPerStockCode.filter { it.value > 10 }.toList().sortedByDescending { it.second }
        if (suspiciousStocks.isNotEmpty()) {
            println("많은 데이터를 처리한 상위 종목:")
            suspiciousStocks.take(5).forEach { (code, count) ->
                log.warn("  - $code: $count 개 레코드")
            }
        }

        return Pair(stockPriceSaveList, stockPriceUpdateList)
    }
}
