package com.zeki.stockdata.service.stock_price

import com.zeki.mole_tunnel_db.entity.StockPrice
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import com.zeki.mole_tunnel_db.repository.join.StockPriceJoinRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class StockPriceVolumeIndicatorService(
    private val stockPriceService: StockPriceService,
    private val stockPriceRepository: StockPriceRepository,
    private val stockPriceJoinRepository: StockPriceJoinRepository,
) {
    private val log = mu.KotlinLogging.logger {}

    /** 볼륨 관련 지표 업데이트 함수 - 효율적 맵 활용 버전 */
    fun updateVolumeIndicators(stockCodeList: List<String>, standardDate: LocalDate): Int {
        if (stockCodeList.isEmpty()) {
            return 0
        }

        log.info("볼륨 관련 지표 업데이트 시작: 대상 종목 수 = ${stockCodeList.size}, 기준일 = $standardDate")

        var totalUpdated = 0

        // 지표 계산에 필요한 과거 30일 데이터 조회 (20일 평균 + 여유분)
        val startDate = standardDate.minusDays(30)

        // 데이터 조회 및 처리 시점 측정

        // 1. 한 번에 모든 데이터 조회 - 데이터베이스 부하 감소
        val allStockPrices =
            stockPriceRepository.findAllByDateBetweenAndStockInfoCodeIn(
                startDate = startDate,
                endDate = standardDate,
                stockCodeList = stockCodeList
            )

        // 데이터가 없으면 조기 종료
        if (allStockPrices.isEmpty()) {
            log.info("${startDate}~${standardDate} 기간 동안 조회된 주가 데이터가 없습니다")
            return 0
        }

        log.info("${startDate}~${standardDate} 기간 동안 ${allStockPrices.size}개의 주가 데이터 조회됨")

        // 2. 효율적인 맵 구조로 데이터 변환 (종목코드 → 날짜 → 주가 데이터)
        val stockPriceMap = buildStockPriceMap(allStockPrices)

        // 3. 기준일 데이터만 추출 - 처리 대상 확인
        val targetStockPrices = extractTargetStockPrices(stockPriceMap, standardDate)

        if (targetStockPrices.isEmpty()) {
            log.info("${standardDate} 날짜에 해당하는 주가 데이터가 없습니다")
            return 0
        }

        log.info("${standardDate} 날짜 데이터 처리 중: ${targetStockPrices.size}개 종목")

        // 4. 볼륨 지표 계산 및 업데이트 대상 선정
        val stockPricesToUpdate =
            calculateVolumeIndicators(
                targetStockPrices = targetStockPrices,
                stockPriceMap = stockPriceMap,
                standardDate = standardDate
            )

        val failedCount = targetStockPrices.size - stockPricesToUpdate.size
        if (failedCount > 0) {
            log.info(
                "처리 불가한 종목 수: $failedCount (${100 * failedCount / targetStockPrices.size}%)"
            )
        }

        // 5. 벌크 업데이트 수행
        if (stockPricesToUpdate.isNotEmpty()) {
            // 대용량 데이터 처리를 위해 더 작은 배치 사이즈로 나눠서 처리
            val batchSize = 1000
            stockPricesToUpdate.chunked(batchSize).forEach { chunk ->
                stockPriceJoinRepository.bulkUpdateVolumeIndicatorsByValues(chunk)
                totalUpdated += chunk.size
                log.info(
                    "${chunk.size}개 데이터 업데이트 완료 (총 ${totalUpdated}/${stockPricesToUpdate.size})"
                )
            }
        }

        return totalUpdated
    }

    /** 주가 데이터를 효율적인 맵 구조로 변환 (종목코드 → 날짜 → 주가 데이터) */
    private fun buildStockPriceMap(
        stockPrices: List<StockPrice>
    ): Map<String, Map<LocalDate, StockPrice>> {
        // 시작 시간 측정
        val startTime = System.currentTimeMillis()

        // 맵 구성 - 종목코드 → 날짜 → 주가 데이터
        val result = mutableMapOf<String, MutableMap<LocalDate, StockPrice>>()

        // 단일 반복문으로 모든 데이터 처리 - O(n) 시간복잡도
        stockPrices.forEach { stockPrice ->
            val code = stockPrice.stockInfo.code
            result.getOrPut(code) { mutableMapOf() }.put(stockPrice.date, stockPrice)
        }

        val elapsedTime = System.currentTimeMillis() - startTime
        log.info("주가 데이터 맵 구성 완료: ${result.size}개 종목, ${elapsedTime}ms 소요")

        return result
    }

    /** 기준일에 해당하는 주가 데이터 추출 */
    private fun extractTargetStockPrices(
        stockPriceMap: Map<String, Map<LocalDate, StockPrice>>,
        standardDate: LocalDate
    ): List<StockPrice> {
        val result = mutableListOf<StockPrice>()

        // 각 종목별로 기준일 데이터 추출
        stockPriceMap.forEach { (_, pricesByDate) ->
            pricesByDate[standardDate]?.let { result.add(it) }
        }

        return result
    }

    /** 볼륨 지표 계산 및 업데이트 대상 선정 함수 */
    private fun calculateVolumeIndicators(
        targetStockPrices: List<StockPrice>,
        stockPriceMap: Map<String, Map<LocalDate, StockPrice>>,
        standardDate: LocalDate
    ): List<StockPrice> {
        val stockPricesToUpdate = mutableListOf<StockPrice>()
        var processedCount = 0
        var emptyDataCount = 0

        log.info("볼륨 지표 계산 시작: 대상 종목 수 = ${targetStockPrices.size}")

        // 각 종목별로 처리
        for (currentPrice in targetStockPrices) {
            processedCount++
            val stockCode = currentPrice.stockInfo.code
            val pricesByDate = stockPriceMap[stockCode] ?: continue

            // 볼륨 지표 계산에 필요한 캐시 데이터 준비
            val indicatorData =
                prepareVolumeIndicatorData(
                    currentPrice = currentPrice,
                    pricesByDate = pricesByDate,
                    targetDate = standardDate
                )

            // 데이터가 불충분하면 다음 종목으로
            if (indicatorData.isEmpty()) {
                emptyDataCount++
                if (emptyDataCount % 100 == 0 || emptyDataCount <= 10) {
                    log.debug("${stockCode} 종목의 데이터가 불충분하여 계산 불가 (총 ${emptyDataCount}개)")
                }
                continue
            }

            // 지표 계산 및 업데이트 여부 확인
            val updated = updateStockPriceIndicators(currentPrice, indicatorData)
            if (updated) stockPricesToUpdate.add(currentPrice)

            // 주기적으로 진행 상황 로깅
            if (processedCount % 1000 == 0) {
                log.info(
                    "볼륨 지표 계산 진행 중: ${processedCount}/${targetStockPrices.size} 종목 처리 완료 (${stockPricesToUpdate.size}개 업데이트 예정)"
                )
            }
        }

        log.info(
            "볼륨 지표 계산 완료: 총 ${targetStockPrices.size}개 종목 중 ${stockPricesToUpdate.size}개 업데이트 예정"
        )
        return stockPricesToUpdate
    }

    /** 볼륨 지표 계산에 필요한 데이터 준비 함수 */
    private fun prepareVolumeIndicatorData(
        currentPrice: StockPrice,
        pricesByDate: Map<LocalDate, StockPrice>,
        targetDate: LocalDate
    ): Map<String, Any> {
        // 최소 20일 이상의 과거 데이터가 필요함
        val previous20Days = (1..20).map { targetDate.minusDays(it.toLong()) }

        // 가용한 데이터 확인
        val availableDates = previous20Days.filter { pricesByDate.containsKey(it) }
        if (availableDates.size < 5) {
            // 최소 5일 데이터 없으면 처리 불가
            return emptyMap()
        }

        // 이전 날짜 데이터 추출 및 정렬
        val availablePrices =
            availableDates.mapNotNull { pricesByDate[it] }.sortedByDescending { it.date }

        // 5일 및 최대 20일 데이터 선택
        val previous5DaysPrices = availablePrices.take(5)
        val previous20DaysPrices = availablePrices.take(availablePrices.size.coerceAtMost(20))

        // 거래량 데이터 추출
        val volumeSum5 = previous5DaysPrices.sumOf { it.volume }
        val volumeSum20 = previous20DaysPrices.sumOf { it.volume }

        // 직전 종가 (가격 변동률 계산용)
        val previousClose = previous20DaysPrices.first().close

        return mapOf(
            "volumeSum5" to volumeSum5,
            "volumeSum20" to volumeSum20,
            "previous5DaysCount" to previous5DaysPrices.size,
            "previous20DaysCount" to previous20DaysPrices.size,
            "previousClose" to previousClose
        )
    }

    /** 볼륨 지표 계산 및 주가 데이터 업데이트 */
    private fun updateStockPriceIndicators(
        currentPrice: StockPrice,
        indicatorData: Map<String, Any>
    ): Boolean {
        try {
            // 캐시에서 데이터 추출
            val volumeSum5 = indicatorData["volumeSum5"] as Long
            val volumeSum20 = indicatorData["volumeSum20"] as Long
            val previous5DaysCount = indicatorData["previous5DaysCount"] as Int
            val previous20DaysCount = indicatorData["previous20DaysCount"] as Int
            val previousClose = indicatorData["previousClose"] as BigDecimal

            // 5일 평균 거래량 계산
            val volumeAvg5 =
                BigDecimal.valueOf(volumeSum5)
                    .divide(
                        BigDecimal.valueOf(previous5DaysCount.toLong()),
                        0,
                        RoundingMode.HALF_UP
                    )

            // 실제 데이터 개수 기준으로 평균 거래량 계산
            val volumeAvg20 =
                BigDecimal.valueOf(volumeSum20)
                    .divide(
                        BigDecimal.valueOf(previous20DaysCount.toLong()),
                        0,
                        RoundingMode.HALF_UP
                    )

            // 거래량 비율 계산 (현재 거래량 / 평균 거래량)
            val volumeRatio =
                if (volumeAvg20 > BigDecimal.ZERO) {
                    BigDecimal.valueOf(currentPrice.volume)
                        .divide(volumeAvg20, 4, RoundingMode.HALF_UP)
                } else {
                    BigDecimal.ONE // 0으로 나누기 방지
                }

            // 가격 변동률 계산
            val priceChangeRate =
                if (previousClose > BigDecimal.ZERO) {
                    currentPrice
                        .close
                        .subtract(previousClose)
                        .divide(previousClose, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal("100"))
                } else {
                    BigDecimal.ZERO
                }

            // 변동성 계산
            val volatility =
                if (currentPrice.open > BigDecimal.ZERO) {
                    currentPrice
                        .high
                        .subtract(currentPrice.low)
                        .divide(currentPrice.open, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal("100"))
                } else {
                    BigDecimal.ZERO
                }

            // 업데이트 필요 여부 확인 및 적용
            return currentPrice.updateVolumeIndicators(
                volumeAvg5 = volumeAvg5,
                volumeAvg20 = volumeAvg20,
                volumeRatio = volumeRatio,
                priceChangeRate = priceChangeRate,
                volatility = volatility
            )
        } catch (e: Exception) {
            log.error("볼륨 지표 계산 중 오류 발생: ${e.message}, 종목코드: ${currentPrice.stockInfo.code}")
            return false
        }
    }

}