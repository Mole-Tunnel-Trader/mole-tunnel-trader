package com.zeki.kisvolkotlin.domain.kis.stock_price

import com.zeki.kisvolkotlin.db.repository.StockPriceJoinRepository
import com.zeki.kisvolkotlin.db.repository.StockPriceRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StockPriceService(
    private val stockPriceRepository: StockPriceRepository,
    private val stockPriceJoinRepository: StockPriceJoinRepository,

    private val crawlNaverFinanceService: CrawlNaverFinanceService
) {

    fun upsertStockPrice(
        stockCode: String,
        standardDate: LocalDate,
        count: Int
    ) {

        val crawlStockPriceDto = crawlNaverFinanceService.crawlStockPrice(stockCode, standardDate, count)

    }
}