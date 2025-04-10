package com.zeki.stockdata.service.stock_price

import com.zeki.common.dto.UpsertReportDto
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.mole_tunnel_db.entity.StockPrice
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import com.zeki.mole_tunnel_db.repository.join.StockPriceJoinRepository
import com.zeki.stockdata.service.stock_info.CrawlNaverFinanceService
import com.zeki.stockdata.service.stock_info.StockInfoService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.system.measureTimeMillis

@Service
class StockPriceService(
    private val stockPriceRepository: StockPriceRepository,
    private val stockPriceJoinRepository: StockPriceJoinRepository,
    private val stockInfoService: StockInfoService,
    private val crawlNaverFinanceService: CrawlNaverFinanceService,
    private val stockPriceDataService: StockPriceDataService
) {
    private val log = mu.KotlinLogging.logger {}

    // 배치 처리 사이즈 설정
    private val batchSize = 500

    fun upsertStockPrice(
        stockCodeList: List<String>,
        standardDate: LocalDate,
        count: Int
    ): UpsertReportDto {
        // 결과를 합산하기 위한 변수들
        val totalStockPriceSaveList = mutableListOf<StockPrice>()
        val totalStockPriceUpdateList = mutableListOf<StockPrice>()

        try {
            // 500개씩 나누어 처리
            stockCodeList.chunked(batchSize).forEach { batchCodes ->
                // 1. 데이터 준비 트랜잭션에서 데이터 조회 및 처리
                val (stockPriceSaveList, stockPriceUpdateList) =
                    stockPriceDataService.prepareStockPriceData(batchCodes, standardDate, count)

                // 결과 리스트에 추가
                totalStockPriceSaveList.addAll(stockPriceSaveList)
                totalStockPriceUpdateList.addAll(stockPriceUpdateList)
            }

            // 2. 모든 배치의 결과를 모아서 한 번에 벌크 작업 실행
            stockPriceJoinRepository.bulkInsert(totalStockPriceSaveList)
            stockPriceJoinRepository.bulkUpdate(totalStockPriceUpdateList)

            // 3. 결과 보고서 반환
            log.info(
                "주가 데이터 벌크 업데이트 완료: 삽입 ${totalStockPriceSaveList.size}건, 업데이트 ${totalStockPriceUpdateList.size}건"
            )
            return UpsertReportDto(totalStockPriceSaveList.size, totalStockPriceUpdateList.size, 0)
        } catch (e: Exception) {
            log.error("주가 데이터 upsert 중 오류 발생: ${e.message}", e)

            // 오류 발생 시에도 부분적으로 성공한 처리 수를 반환
            return UpsertReportDto(totalStockPriceSaveList.size, totalStockPriceUpdateList.size, 0)
        }
    }

    fun updateRsi(stockCodeList: List<String>, standardDate: LocalDate) {
        log.info("RSI 업데이트 시작: 대상 종목 수 = ${stockCodeList.size}")

        // 기준일 이전 30일간의 데이터 필요 (RSI 계산용)
        val startDate = standardDate.minusDays(30)

        // 모든 종목의 데이터를 한 번에 조회
        val allStockPrices =
            stockPriceRepository.findAllByDateBetweenAndStockInfoCodeIn(
                startDate = startDate,
                endDate = standardDate,
                stockCodeList = stockCodeList
            )

        log.info("${startDate}~${standardDate} 기간 동안 ${allStockPrices.size}개의 주가 데이터 조회됨")

        // 종목코드별로 그룹화 및 날짜순 정렬
        val stockPricesByCode =
            allStockPrices.groupBy { it.stockInfo.code }.mapValues { (_, prices) ->
                prices.sortedBy { it.date }
            }

        // 업데이트할 StockPrice 리스트
        val stockPricesToUpdate = mutableListOf<StockPrice>()

        // 각 종목별로 RSI 계산
        stockPricesByCode.forEach { (stockCode, stockPrices) ->
            // 최소 15일치 데이터가 필요 (14일 + 당일)
            if (stockPrices.size < 15) {
                log.debug("$stockCode: RSI 계산을 위한 데이터 부족 (${stockPrices.size}개), 계산 건너뜀")
                return@forEach
            }

            // 주가 데이터 순회하며 RSI 계산
            for (i in 14 until stockPrices.size) {
                val currentPrice = stockPrices[i]
                val periodData = stockPrices.subList(i - 14, i)

                // 일간 가격 변화량 계산
                val differences = periodData.map { (it.close - it.open).toLong() }

                // 상승/하락 분리
                val gains = differences.filter { it > 0 }
                val losses = differences.filter { it < 0 }.map { kotlin.math.abs(it) } // 절댓값으로 전환

                // 평균 상승/하락 계산
                val avgGain = if (gains.isNotEmpty()) gains.sum().toDouble() / 14 else 0.0
                val avgLoss = if (losses.isNotEmpty()) losses.sum().toDouble() / 14 else 0.0

                // RSI 계산
                val rsi =
                    if (avgLoss == 0.0) {
                        // 손실이 없으므로 상승만 있었던 경우 => RSI는 100에 근접
                        100f
                    } else {
                        val rs = avgGain / avgLoss
                        (100.0 - (100.0 / (1.0 + rs))).toFloat()
                    }

                // 업데이트가 필요한지 확인하고 리스트에 추가
                if (currentPrice.updateRsi(rsi)) {
                    stockPricesToUpdate.add(currentPrice)
                }
            }
        }

        // 벌크 업데이트 수행
        if (stockPricesToUpdate.isNotEmpty()) {
            log.info("RSI 데이터 ${stockPricesToUpdate.size}개를 bulk 업데이트 시작")
            stockPriceJoinRepository.bulkUpdateRsiByValues(stockPricesToUpdate)
            log.info("RSI 데이터 bulk 업데이트 완료")
        } else {
            log.info("업데이트할 RSI 데이터가 없습니다")
        }
    }

    /** 볼륨 관련 지표 업데이트 함수 - 성능 개선 버전 */
    fun updateVolumeIndicators(stockCodeList: List<String>, standardDate: LocalDate): Int {
        if (stockCodeList.isEmpty()) {
            return 0
        }

        log.info("볼륨 관련 지표 업데이트 시작: 대상 종목 수 = ${stockCodeList.size}, 기준일 = $standardDate")

        var totalUpdated = 0
        val executionTime = measureTimeMillis {
            // 지표 계산에 필요한 과거 30일 데이터 조회 (20일 평균 + 여유분)
            val startDate = standardDate.minusDays(30)

            // 데이터 조회 시점 측정
            val queryTime = measureTimeMillis {
                // 날짜별 데이터를 한 번에 가져오기 - 데이터베이스 부하 감소
                // stock_price_stock_info_id_date_index 인덱스 활용
                val allStockPrices =
                    stockPriceRepository.findAllByDateBetweenAndStockInfoCodeIn(
                        startDate = startDate,
                        endDate = standardDate,
                        stockCodeList = stockCodeList
                    )

                // 데이터가 없으면 조기 종료
                if (allStockPrices.isEmpty()) {
                    log.info("${startDate}~${standardDate} 기간 동안 조회된 주가 데이터가 없습니다")
                    totalUpdated = 0
                    return@measureTimeMillis
                }

                log.info("${startDate}~${standardDate} 기간 동안 ${allStockPrices.size}개의 주가 데이터 조회됨")

                // 날짜 인덱스 활용을 위한 멀티맵 구조체 생성 - O(n) 시간 복잡도로 빠른 검색 가능
                val stockPricesByCodeAndDate =
                    mutableMapOf<String, MutableMap<LocalDate, StockPrice>>()
                allStockPrices.forEach { stockPrice ->
                    val code = stockPrice.stockInfo.code
                    stockPricesByCodeAndDate
                        .getOrPut(code) { mutableMapOf() }
                        .put(stockPrice.date, stockPrice)
                }

                // 기준일 데이터만 필터링 - 필요한 데이터만 처리하기 위함
                val stockPricesOnDate = allStockPrices.filter { it.date == standardDate }

                if (stockPricesOnDate.isEmpty()) {
                    log.info("${standardDate} 날짜에 해당하는 주가 데이터가 없습니다")
                    totalUpdated = 0
                    return@measureTimeMillis
                }

                log.info("${standardDate} 날짜 데이터 처리 중: ${stockPricesOnDate.size}개 종목")

                // 효율적인 데이터 처리를 위해 Map 구조로 데이터 준비
                val stockPricesToUpdate =
                    processVolumeIndicatorsOptimized(
                        stockPricesOnDate,
                        stockPricesByCodeAndDate,
                        standardDate
                    )

                // 벌크 업데이트 수행
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

                log.info("볼륨 지표 업데이트 완료: 총 $totalUpdated 건")
            }

            log.info("볼륨 지표 업데이트 쿼리 및 처리 시간: ${queryTime}ms")
        }

        log.info("볼륨 지표 업데이트 총 실행 시간: ${executionTime}ms")
        return totalUpdated
    }

    /** 볼륨 지표를 계산하는 최적화된 함수 - 멀티맵 구조체 활용 */
    private fun processVolumeIndicatorsOptimized(
        stockPricesOnDate: List<StockPrice>,
        stockPricesByCodeAndDate: Map<String, Map<LocalDate, StockPrice>>,
        targetDate: LocalDate
    ): List<StockPrice> {
        // 업데이트할 StockPrice 리스트
        val stockPricesToUpdate = mutableListOf<StockPrice>()

        // 각 종목별로 처리
        for (currentPrice in stockPricesOnDate) {
            val stockCode = currentPrice.stockInfo.code
            val pricesByDate = stockPricesByCodeAndDate[stockCode] ?: continue

            // 볼륨 지표 계산에 필요한 캐시 데이터 준비
            val cacheData =
                prepareVolumeIndicatorDataOptimized(currentPrice, pricesByDate, targetDate)

            // 캐시 데이터가 비어있으면 계산 불가
            if (cacheData.isEmpty()) continue

            // 지표 계산 및 업데이트 여부 확인
            val updated = calculateAndUpdateVolumeIndicators(currentPrice, cacheData)
            if (updated) stockPricesToUpdate.add(currentPrice)
        }

        return stockPricesToUpdate
    }

    /** 볼륨 지표 계산에 필요한 데이터 준비 함수 - Map 활용 최적화 버전 */
    private fun prepareVolumeIndicatorDataOptimized(
        currentPrice: StockPrice,
        pricesByDate: Map<LocalDate, StockPrice>,
        targetDate: LocalDate
    ): Map<String, Any> {
        // 최소 20일 이상의 과거 데이터가 필요함
        val previous20Days = (1..20).map { targetDate.minusDays(it.toLong()) }

        // 모든 필요한 날짜의 데이터가 있는지 확인
        if (previous20Days.any { !pricesByDate.containsKey(it) }) {
            return emptyMap()
        }

        // 이전 5일 및 20일 데이터 추출
        val previous5DaysPrices = previous20Days.take(5).mapNotNull { pricesByDate[it] }
        val previous20DaysPrices = previous20Days.mapNotNull { pricesByDate[it] }

        // 충분한 데이터가 없으면 처리 불가
        if (previous5DaysPrices.size < 5 || previous20DaysPrices.size < 20) {
            return emptyMap()
        }

        // 5일 평균 거래량 계산용 데이터
        val previous5DaysVolumes = previous5DaysPrices.map { it.volume }
        val volumeSum5 = previous5DaysVolumes.sum()

        // 20일 평균 거래량 계산용 데이터
        val previous20DaysVolumes = previous20DaysPrices.map { it.volume }
        val volumeSum20 = previous20DaysVolumes.sum()

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

    /** 볼륨 지표를 계산하고 업데이트하는 함수 */
    private fun calculateAndUpdateVolumeIndicators(
        currentPrice: StockPrice,
        cacheData: Map<String, Any>
    ): Boolean {
        // 캐시에서 데이터 추출
        val volumeSum5 = cacheData["volumeSum5"] as Long
        val volumeSum20 = cacheData["volumeSum20"] as Long
        val previous5DaysCount = cacheData["previous5DaysCount"] as Int
        val previous20DaysCount = cacheData["previous20DaysCount"] as Int
        val previousClose = cacheData["previousClose"] as BigDecimal

        // 5일 평균 거래량 계산
        val volumeAvg5 =
            BigDecimal.valueOf(volumeSum5)
                .divide(
                    BigDecimal.valueOf(previous5DaysCount.toLong()),
                    0,
                    RoundingMode.HALF_UP
                )

        // 20일 평균 거래량 계산
        val volumeAvg20 =
            BigDecimal.valueOf(volumeSum20)
                .divide(
                    BigDecimal.valueOf(previous20DaysCount.toLong()),
                    0,
                    RoundingMode.HALF_UP
                )

        // 거래량 비율 계산 (현재 거래량 / 20일 평균 거래량)
        val volumeRatio =
            if (volumeAvg20 > BigDecimal.ZERO) {
                BigDecimal.valueOf(currentPrice.volume)
                    .divide(volumeAvg20, 4, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ONE // 나누기 연산 0 방지
            }

        // 가격 변동률 계산 ((종가 - 전일 종가) / 전일 종가 * 100)
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

        // 변동성 계산 ((고가 - 저가) / 시가 * 100)
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

        // 업데이트가 필요한지 확인하고 적용
        return currentPrice.updateVolumeIndicators(
            volumeAvg5 = volumeAvg5,
            volumeAvg20 = volumeAvg20,
            volumeRatio = volumeRatio,
            priceChangeRate = priceChangeRate,
            volatility = volatility
        )
    }

    @Transactional(readOnly = true)
    fun getStockPriceByDate(baseDate: LocalDate, stockCodeList: List<String>): List<StockPrice> {
        return stockPriceRepository.findAllByStockInfo_CodeInAndDate(
            stockCodeList = stockCodeList,
            date = baseDate
        )
    }

    @Transactional(readOnly = true)
    fun getStockPriceList(
        startLocalDate: LocalDate,
        endLocalDate: LocalDate,
        stockCodeList: List<String>
    ): List<StockPrice> {
        return stockPriceRepository.findAllByDateBetweenAndStockInfoCodeIn(
            startDate = startLocalDate,
            endDate = endLocalDate,
            stockCodeList = stockCodeList
        )
    }

    @Transactional(readOnly = true)
    fun getStockPriceList(
        startLocalDate: LocalDate,
        stockCodeList: List<String>
    ): List<StockPrice> {
        return stockPriceRepository.findAllByDateGreaterThanEqualAndStockInfoCodeInOrderByDateAsc(
            baseDate = startLocalDate,
            stockCodeList = stockCodeList
        )
    }

    @Transactional(readOnly = true)
    fun getStockPriceListByDate(
        baseDate: LocalDate,
        stockCodeList: List<String>
    ): List<StockPrice> {
        return stockPriceRepository.findAllByStockInfo_CodeInAndDate(
            stockCodeList = stockCodeList,
            date = baseDate
        )
    }

    /**
     * 거래량 급증 종목 조회
     * @param date 기준 날짜
     * @param minVolumeRatio 최소 거래량 비율 (기본값: 1.5)
     * @param priceIncreaseOnly 가격 상승 종목만 조회할지 여부 (기본값: false)
     * @return 거래량 급증 종목 리스트
     */
    @Transactional(readOnly = true)
    fun getVolumeSpikes(
        date: LocalDate,
        minVolumeRatio: BigDecimal = BigDecimal("1.5"),
        priceIncreaseOnly: Boolean = false
    ): List<StockPrice> {
        val stockPrices = stockPriceRepository.findAllByDateOrderByStockInfoCode(date)

        return stockPrices
            .filter { stockPrice ->
                // 거래량 비율이 기준치 이상인 종목만 필터링
                stockPrice.volumeRatio != null &&
                        stockPrice.volumeRatio!! >= minVolumeRatio &&
                        // 가격 상승 종목만 조회하는 경우 추가 필터링
                        (!priceIncreaseOnly ||
                                (stockPrice.priceChangeRate != null &&
                                        stockPrice.priceChangeRate!! > BigDecimal.ZERO))
            }
            .sortedByDescending { it.volumeRatio ?: BigDecimal.ZERO }
    }

    /**
     * 변동성이 높은 종목 조회
     * @param date 기준 날짜
     * @param minVolumeRatio 최소 거래량 비율
     * @param minVolatility 최소 변동성
     * @return 변동성이 높은 종목 리스트
     */
    @Transactional(readOnly = true)
    fun getHighVolatilityStocks(
        date: LocalDate,
        minVolumeRatio: BigDecimal = BigDecimal("1.3"),
        minVolatility: BigDecimal = BigDecimal("0.03")
    ): List<StockPrice> {
        val stockPrices = stockPriceRepository.findAllByDateOrderByStockInfoCode(date)

        return stockPrices
            .filter { stockPrice ->
                // 거래량 비율이 기준치 이상이고 변동성이 기준치 이상인 종목만 필터링
                stockPrice.volumeRatio != null &&
                        stockPrice.volumeRatio!! >= minVolumeRatio &&
                        stockPrice.volatility != null &&
                        stockPrice.volatility!! >= minVolatility
            }
            .sortedByDescending { it.volatility ?: BigDecimal.ZERO }
    }

    @Transactional(readOnly = true)
    fun getStockPriceOne(date: LocalDate, stockCode: String): StockPrice {
        val stockCodeList = stockPriceRepository.findAllByStockInfo_CodeInAndDate(listOf(stockCode), date)

        if (stockCodeList.isEmpty()) {
            throw ApiException(ResponseCode.RESOURCE_NOT_FOUND, "해당 종목의 주가가 존재하지 않습니다.")
        }
        return stockCodeList[0]
    }
}