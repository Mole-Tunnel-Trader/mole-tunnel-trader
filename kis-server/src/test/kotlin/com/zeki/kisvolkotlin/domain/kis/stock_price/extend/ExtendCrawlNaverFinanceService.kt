package com.zeki.kisvolkotlin.domain.kis.stock_price.extend

import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import com.zeki.kisvolkotlin.domain.kis.stock_price.CrawlNaverFinanceService
import com.zeki.kisvolkotlin.domain.kis.stock_price.dto.NaverStockPriceResDto
import com.zeki.kisvolkotlin.utils.TestUtils
import java.time.LocalDate

class ExtendCrawlNaverFinanceService(
    webClientConnector: WebClientConnector
) : CrawlNaverFinanceService(
    webClientConnector
) {

    override fun crawlStockPrice(stockCode: String, stdDay: LocalDate, count: Int): NaverStockPriceResDto {
        val filePath000020 = "src/test/resources/stock_price/string/20240327_10_000020.txt"
        val filePath000040 = "src/test/resources/stock_price/string/20240327_10_000040.txt"

        val response = when (stockCode) {
            "000020" -> TestUtils.loadString(filePath000020)
            "000040" -> TestUtils.loadString(filePath000040)
            else -> throw IllegalArgumentException("해당 데이터에 맞는 파일이 없습니다. stockCode: $stockCode")
        }

        return this.toNaverDto(response, stockCode)
    }

}