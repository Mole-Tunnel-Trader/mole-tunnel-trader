package com.zeki.kisvolkotlin.domain.kis.stock_price

import com.zeki.kisserver.db.repository.StockPriceJoinRepository
import com.zeki.kisserver.db.repository.StockPriceRepository
import com.zeki.kisserver.domain._common.webclient.WebClientConnector
import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import com.zeki.kisvolkotlin.db.entity.StockInfo
import com.zeki.kisvolkotlin.db.repository.StockPriceJoinRepository
import com.zeki.kisvolkotlin.db.repository.StockPriceRepository
import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import com.zeki.kisvolkotlin.domain.kis.stock_info.StockInfoService
import com.zeki.kisvolkotlin.domain.kis.stock_price.extend.ExtendCrawlNaverFinanceService
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Suppress("LocalVariableName")
@Transactional
@ActiveProfiles("test")
@SpringBootTest
class StockPriceServiceTest(
    @Autowired private var stockPriceRepository: StockPriceRepository,
    @Autowired private var stockPriceJoinRepository: StockPriceJoinRepository,

    @Autowired private var stockInfoRepository: com.zeki.kisserver.db.repository.StockInfoRepository,

    @Autowired private var em: EntityManager,

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
        fun `upsertStockPrice() - 동작 테스트`() {
            // given
            StockInfo(
                code = "000020",
                name = "동화약품",
                otherCode = "27931470",
                fcamt = 1000,
                amount = 27931470,
                marketCapital = 2673,
                capital = 279,
                per = 9.75,
                pbr = 0.72,
                eps = 982.0
            ).let { stockInfoRepository.save(it) }

            StockInfo(
                code = "000040",
                name = "KR모터스",
                otherCode = "29132868",
                fcamt = 500,
                amount = 29132868,
                marketCapital = 306,
                capital = 146,
                per = -4.59,
                pbr = 0.92,
                eps = -229.0
            ).let { stockInfoRepository.save(it) }

            val stockCodeList = listOf("000020", "000040")
            val standardDate = LocalDate.of(2024, 3, 27)
            val count = 10

            // when
            extendStockPriceService.upsertStockPrice(stockCodeList, standardDate, count)

            em.clear()  // bulkInsert 이므로 영속성 컨텍스트 초기화
            val stockPriceList = stockPriceRepository.findByStockInfoCodeIn(stockCodeList)
            val 동화약품 = stockPriceList.stream()
                .filter { it.stockInfo.code == "000020" && it.date == standardDate }
                .findFirst()
                .get()
            val KR모터스 = stockPriceList.stream()
                .filter { it.stockInfo.code == "000040" && it.date == standardDate }
                .findFirst()
                .get()

            // then
            assertAll(
                { assertEquals(standardDate, 동화약품.date) },
                { assertEquals(9460, 동화약품.close.toInt()) },
                { assertEquals(52878, 동화약품.volume) },

                { assertEquals(standardDate, KR모터스.date) },
                { assertEquals(1013, KR모터스.close.toInt()) },
                { assertEquals(170230, KR모터스.volume) },
            )
        }

        // TODO : 업데이트, 삭제 테스트 추가
    }
}
