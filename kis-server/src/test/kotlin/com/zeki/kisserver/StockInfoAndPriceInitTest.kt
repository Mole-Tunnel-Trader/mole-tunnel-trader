package com.zeki.kisserver

import com.zeki.kisserver.domain.data_go.stock_code.StockCodeService
import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Commit
import java.time.LocalDate

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Commit
class StockInfoAndPriceServiceTest {

    @Autowired
    private lateinit var stockCodeService: StockCodeService

    @Autowired
    private lateinit var stockInfoService: StockInfoService

    @Autowired
    private lateinit var stockPriceService: StockPriceService

    @Test
    fun `주식 정보와 5년치 가격을 생성 및 업데이트 한다`() {
        // Given
        val stockCodeList = stockCodeService.getStockCodeList()

        // When
        val upsertInfo = stockInfoService.upsertStockInfo(stockCodeList)
        val upsertPrice = stockPriceService.upsertStockPrice(stockCodeList, LocalDate.now(), 5)

    }
}
