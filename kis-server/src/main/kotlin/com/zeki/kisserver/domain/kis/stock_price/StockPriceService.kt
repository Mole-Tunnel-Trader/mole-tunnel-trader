package com.zeki.kisserver.domain.kis.stock_price


import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.stockdata.stock_price.StockPrice
import com.zeki.stockdata.stock_price.StockPriceJoinRepository
import com.zeki.stockdata.stock_price.StockPriceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class StockPriceService(
    private val stockPriceRepository: StockPriceRepository,
    private val stockPriceJoinRepository: StockPriceJoinRepository,

    private val stockInfoService: StockInfoService,

    private val crawlNaverFinanceService: CrawlNaverFinanceService
) {

    @Transactional
    fun upsertStockPrice(
        stockCodeList: List<String>,
        standardDate: LocalDate,
        count: Int
    ) {
        val stockPriceSaveList = mutableListOf<StockPrice>()
        val stockPriceUpdateList = mutableListOf<StockPrice>()
        val stockPriceDeleteSet = mutableSetOf<StockPrice>()

        val stockInfoList = stockInfoService.getStockInfoList(stockCodeList)

        for (stockInfo in stockInfoList) {
            val code = stockInfo.code
            val stockPriceMap = stockInfo.stockPriceList
                .associateBy { it.date }
                .toMutableMap()

            val crawlStockPriceDto = crawlNaverFinanceService.crawlStockPrice(code, standardDate, count)

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
                        val isUpdate = stockPrice.updateStockPrice(
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
            stockPriceDeleteSet.addAll(stockPriceMap.values)
        }

        stockPriceJoinRepository.bulkInsert(stockPriceSaveList)
        stockPriceJoinRepository.bulkUpdate(stockPriceUpdateList)
    }

    @Transactional
    fun updateRsi(
        stockCodeList: List<String>,
        standardDate: LocalDate
    ) {
        val stockPriceList = stockPriceRepository.findAllByDateGreaterThanEqualAndStockInfoCodeInOrderByDateAsc(
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

                rsi = if (avgLoss == 0.0) {
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
    fun getStockPriceByDate(
        baseDate: LocalDate,
        stockCodeList: List<String>
    ): List<StockPrice> {
        return stockPriceRepository.findAllByDateAndStockInfoCodeIn(
            baseDate = baseDate,
            stockCodeList = stockCodeList
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

}