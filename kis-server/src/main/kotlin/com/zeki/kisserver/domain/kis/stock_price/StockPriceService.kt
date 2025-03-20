package com.zeki.kisserver.domain.kis.stock_price

import com.zeki.holiday.dto.report.UpsertReportDto
import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.mole_tunnel_db.entity.StockPrice
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import com.zeki.mole_tunnel_db.repository.join.StockPriceJoinRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

        return UpsertReportDto(totalStockPriceSaveList.size, totalStockPriceUpdateList.size, 0)
    }

    @Transactional
    fun updateRsi(stockCodeList: List<String>, standardDate: LocalDate) {
        val stockPriceList =
            stockPriceRepository.findAllByDateGreaterThanEqualAndStockInfoCodeInOrderByDateAsc(
                baseDate = standardDate,
                stockCodeList = stockCodeList
            )

        for (i in stockPriceList.indices) {
            val stockPrice = stockPriceList[i]

            var rsi: Float? = null
            if (i >= 14) {
                val periodData = stockPriceList.subList(i - 14, i)

                val differences = periodData.map { (it.close - it.open).toLong() }

                val gains = differences.filter { it > 0 }
                val losses = differences.filter { it < 0 }.map { kotlin.math.abs(it) } // 절댓값으로 전환

                val avgGain = if (gains.isNotEmpty()) gains.sum().toDouble() / 14 else 0.0
                val avgLoss = if (losses.isNotEmpty()) losses.sum().toDouble() / 14 else 0.0

                rsi =
                    if (avgLoss == 0.0) {
                        // 손실이 없으므로 상승만 있었던 경우 => RSI는 100에 근접
                        100f
                    } else {
                        val rs = avgGain / avgLoss
                        (100.0 - (100.0 / (1.0 + rs))).toFloat()
                    }
            }

            stockPrice.rsi = rsi
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
}
