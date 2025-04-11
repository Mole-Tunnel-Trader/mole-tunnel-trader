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
import java.time.LocalDate

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
        val stockCodeList =
            stockPriceRepository.findAllByStockInfo_CodeInAndDate(listOf(stockCode), date)

        if (stockCodeList.isEmpty()) {
            throw ApiException(ResponseCode.RESOURCE_NOT_FOUND, "해당 종목의 주가가 존재하지 않습니다.")
        }
        return stockCodeList[0]
    }
}
