package com.zeki.kisvolkotlin.domain.kis.stock_price

import com.zeki.kisvolkotlin.db.repository.StockPriceJoinRepository
import com.zeki.kisvolkotlin.db.repository.StockPriceRepository
import com.zeki.kisvolkotlin.domain._common.util.CustomUtils.toLocalDate
import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import com.zeki.kisvolkotlin.domain.kis.stock_info.StockInfoService
import com.zeki.kisvolkotlin.domain.kis.stock_price.extend.ExtendCrawlNaverFinanceService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@Suppress("LocalVariableName")
@Transactional
@ActiveProfiles("test")
@SpringBootTest
class StockPriceServiceTest(
    @Autowired private var stockPriceRepository: StockPriceRepository,
    @Autowired private var stockPriceJoinRepository: StockPriceJoinRepository,

    @Autowired private var stockInfoService: StockInfoService,
    @Autowired private var webClientConnector: WebClientConnector
) {
    private var extendCrawlNaverFinanceService: ExtendCrawlNaverFinanceService =
        ExtendCrawlNaverFinanceService(webClientConnector)
    private var extendStockPriceService: StockPriceService = StockPriceService(
        stockPriceRepository, stockPriceJoinRepository, stockInfoService, extendCrawlNaverFinanceService
    )

    @Nested
    @DisplayName("성공 테스트")
    inner class SuccessTest {

        @Test
        fun test() {
            // given
            val stockCodeList = listOf("000020", "000040")
            val standardDate = "2024-03-27".toLocalDate("yyyy-MM-dd")
            val count = 10

            // when
            extendStockPriceService.upsertStockPrice(stockCodeList, standardDate, count)

            // then
            stockPriceRepository.findAll().forEach {
                println(it) // TODO : 추후 검증
            }
        }
    }
}
