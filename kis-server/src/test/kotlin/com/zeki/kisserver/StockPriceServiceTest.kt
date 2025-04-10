package com.zeki.kisserver

import com.zeki.common.em.Status
import com.zeki.common.em.StockMarket
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import com.zeki.stockdata.service.stock_info.StockInfoService
import com.zeki.stockdata.service.stock_price.StockPriceService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Commit
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Commit
class StockPriceServiceTest {

    @Autowired
    private lateinit var stockCodeRepository: StockCodeRepository

    @Autowired
    private lateinit var stockPriceRepository: StockPriceRepository

    @Autowired
    private lateinit var stockInfoService: StockInfoService

    @Autowired
    private lateinit var stockPriceService: StockPriceService

    private lateinit var sampleStockCodes: List<String>

    @BeforeEach
    fun setup() {
        // 테스트에 사용할 샘플 주식 코드 준비
        sampleStockCodes = stockCodeRepository.findByIsAliveAndMarketIn(
            Status.Y,
            listOf(StockMarket.KOSPI)
        ).take(3).map { it.code }

        // 주식 정보 업데이트
        stockInfoService.upsertStockInfo(sampleStockCodes)
    }

    @Test
    fun `주식 가격 정보를 성공적으로 업데이트한다`() {
        // Given
        val standardDate = LocalDate.now()
        val count = 10

        // When
        val upsertReport = stockPriceService.upsertStockPrice(sampleStockCodes, standardDate, count)

        // Then
        assertNotNull(upsertReport)
        assertTrue(upsertReport.updateCount >= 0)

        // 주식 가격 정보가 실제로 저장되었는지 확인
        val endDate = standardDate
        val startDate = standardDate.minusDays(count.toLong())
        val stockPriceList = stockPriceService.getStockPriceList(startDate, endDate, sampleStockCodes)

        assertFalse(stockPriceList.isEmpty())
    }

    @Test
    fun `특정 기간 동안의 주식 가격 정보를 정확히 조회한다`() {
        // Given
        val standardDate = LocalDate.now()
        val count = 30
        stockPriceService.upsertStockPrice(sampleStockCodes, standardDate, count)

        val startDate = standardDate.minusDays(15)
        val endDate = standardDate.minusDays(5)

        // When
        val stockPriceList = stockPriceService.getStockPriceList(startDate, endDate, sampleStockCodes)

        // Then
        assertFalse(stockPriceList.isEmpty())

        // 날짜 범위 확인
        stockPriceList.forEach { stockPrice ->
            assertTrue(stockPrice.date >= startDate && stockPrice.date <= endDate)
            assertTrue(sampleStockCodes.contains(stockPrice.stockInfo.code))
        }

        // 주식 가격 데이터 검증
        stockPriceList.forEach { stockPrice ->
            assertNotNull(stockPrice.open)
            assertNotNull(stockPrice.high)
            assertNotNull(stockPrice.low)
            assertNotNull(stockPrice.close)
            assertNotNull(stockPrice.volume)
            assertTrue(stockPrice.high >= stockPrice.low, "최고가는 최저가보다 낮을 수 없습니다")
        }
    }

    @Test
    fun `RSI 지표를 계산하고 업데이트한다`() {
        // Given
        val standardDate = LocalDate.now().minusDays(30)
        stockPriceService.upsertStockPrice(sampleStockCodes, LocalDate.now(), 50)

        // When
        stockPriceService.updateRsi(sampleStockCodes, standardDate)

        // Then
        val stockPriceList = stockPriceService.getStockPriceList(standardDate, sampleStockCodes)
        assertFalse(stockPriceList.isEmpty())

        // 날짜별로 정렬
        val sortedPriceList = stockPriceList.sortedBy { it.date }

        // 14일 이후의 데이터는 RSI 값이 있어야 함
        if (sortedPriceList.size > 14) {
            for (i in 14 until sortedPriceList.size) {
                val stockPrice = sortedPriceList[i]
                assertNotNull(stockPrice.rsi, "인덱스 {$i} RSI 값이 null입니다")
                assertTrue(stockPrice.rsi!! in 0f..100f, "RSI 값이 0에서 100 사이가 아닙니다: ${stockPrice.rsi}")
            }
        }
    }

    @Test
    fun `특정 날짜의 주식 가격 정보를 정확히 조회한다`() {
        // Given
        val targetDate = LocalDate.now().minusDays(5)
        stockPriceService.upsertStockPrice(sampleStockCodes, LocalDate.now(), 10)

        // When
        val stockPriceList = stockPriceService.getStockPriceByDate(targetDate, sampleStockCodes)

        // Then
        if (stockPriceList.isNotEmpty()) {
            stockPriceList.forEach { stockPrice ->
                assertEquals(targetDate, stockPrice.date)
                assertTrue(sampleStockCodes.contains(stockPrice.stockInfo.code))
            }
        }
    }

    @Test
    @Transactional
    fun `주식 가격 데이터의 일관성을 검증한다`() {
        // Given
        val standardDate = LocalDate.now()
        val count = 20
        stockPriceService.upsertStockPrice(sampleStockCodes, standardDate, count)

        // When
        val startDate = standardDate.minusDays(count.toLong())
        val stockPriceList = stockPriceService.getStockPriceList(startDate, standardDate, sampleStockCodes)

        // Then
        assertFalse(stockPriceList.isEmpty())

        // 주식 코드별로 그룹화
        val pricesByCode = stockPriceList.groupBy { it.stockInfo.code }

        pricesByCode.forEach { (code, prices) ->
            // 날짜별로 정렬
            val sortedPrices = prices.sortedBy { it.date }

            // 연속된 날짜 데이터 검증 (주말, 공휴일 제외)
            for (i in 1 until sortedPrices.size) {
                val prevPrice = sortedPrices[i - 1]
                val currPrice = sortedPrices[i]

                // 가격 데이터 일관성 검증
                assertTrue(currPrice.high >= currPrice.low, "최고가는 최저가보다 낮을 수 없습니다")
                assertTrue(currPrice.open >= BigDecimal.ZERO, "시가는 음수일 수 없습니다")
                assertTrue(currPrice.close >= BigDecimal.ZERO, "종가는 음수일 수 없습니다")
                assertTrue(currPrice.volume >= 0L, "거래량은 음수일 수 없습니다")

            }
        }
    }
} 