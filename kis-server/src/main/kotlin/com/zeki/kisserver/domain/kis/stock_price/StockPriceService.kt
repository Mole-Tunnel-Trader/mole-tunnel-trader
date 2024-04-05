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
        stockPriceRepository.deleteAllInBatch(stockPriceDeleteSet)

    }
}