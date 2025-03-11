package com.zeki.kisserver

import com.zeki.common.em.Status
import com.zeki.common.em.StockMarket
import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@Commit
class StockInfoAndPriceServiceTest {

    @Autowired
    private lateinit var stockCodeRepository: StockCodeRepository

    @Autowired
    private lateinit var stockInfoService: StockInfoService

    @Autowired
    private lateinit var stockPriceService: StockPriceService

//    @Test
    fun `주식 정보와 5년치 가격을 생성 및 업데이트 한다`() {
        // Given
        val stockMarkets: MutableCollection<StockMarket> = mutableListOf(StockMarket.KOSPI, StockMarket.KOSDAQ)
        val stockCodeList =stockCodeRepository.findByIsAliveAndMarketIn(Status.Y, stockMarkets).map { it.code }

        // When
        val upsertInfo = stockInfoService.upsertStockInfo(stockCodeList)
        val upsertPrice = stockPriceService.upsertStockPrice(stockCodeList, LocalDate.now(), 1200)

    }

    @Test
    fun `기아 주식 정보 5일치 가격을 생성 및 업데이트 한다`() {
        // Given
        val stockCodeList = stockCodeRepository.findByName("기아").map { it.code }

        // When
        val upsertInfo = stockInfoService.upsertStockInfo(stockCodeList)
        val upsertPrice = stockPriceService.upsertStockPrice(stockCodeList, LocalDate.now(), 5)

    }
}
