package com.zeki.kisserver

import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import org.junit.jupiter.api.Assertions.*
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
    private lateinit var stockCodeRepository: StockCodeRepository

    @Autowired
    private lateinit var stockInfoService: StockInfoService

    @Autowired
    private lateinit var stockPriceService: StockPriceService

    @Test
    fun `기아 주식 정보 5일치 가격을 생성 및 업데이트 한다`() {
        // Given
        val stockCodeList = stockCodeRepository.findByName("기아").map { it.code }

        // When
        val upsertInfo = stockInfoService.upsertStockInfo(stockCodeList)
        val upsertPrice = stockPriceService.upsertStockPrice(stockCodeList, LocalDate.now(), 5)

        // Then
        assertNotNull(upsertInfo)
        assertNotNull(upsertPrice)
        assertTrue(upsertInfo.updateCount >= 0)
        assertTrue(upsertPrice.updateCount >= 0)

        // 기아 주식 정보가 존재하는지 확인
        val stockInfoList = stockInfoService.getStockInfoList(stockCodeList)
        assertFalse(stockInfoList.isEmpty())
        assertEquals("기아", stockInfoList[0].name)
    }

    @Test
    fun `특정 기간 동안의 주식 가격 정보를 조회한다`() {
        // Given
        val stockCodeList = stockCodeRepository.findByName("삼성전자").map { it.code }
        val startDate = LocalDate.now().minusDays(30)
        val endDate = LocalDate.now()

        // 데이터 준비 (필요한 경우)
        stockInfoService.upsertStockInfo(stockCodeList)
        stockPriceService.upsertStockPrice(stockCodeList, LocalDate.now(), 30)

        // When
        val stockPriceList = stockPriceService.getStockPriceList(startDate, endDate, stockCodeList)

        // Then
        assertNotNull(stockPriceList)
        assertFalse(stockPriceList.isEmpty())

        // 날짜 범위 확인
        stockPriceList.forEach { stockPrice ->
            assertTrue(stockPrice.date >= startDate && stockPrice.date <= endDate)
            assertEquals(stockCodeList[0], stockPrice.stockInfo.code)
        }
    }

    @Test
    fun `RSI 지표를 업데이트하고 확인한다`() {
        // Given
        val stockCodeList = stockCodeRepository.findByName("현대차").map { it.code }
        val standardDate = LocalDate.now().minusDays(20)

        // 데이터 준비
        stockInfoService.upsertStockInfo(stockCodeList)
        stockPriceService.upsertStockPrice(stockCodeList, LocalDate.now(), 30)

        // When
        stockPriceService.updateRsi(stockCodeList, standardDate)
        val stockPriceList = stockPriceService.getStockPriceList(standardDate, stockCodeList)

        // Then
        assertNotNull(stockPriceList)
        assertFalse(stockPriceList.isEmpty())

        // 14일 이후의 데이터는 RSI 값이 있어야 함
        val sortedPriceList = stockPriceList.sortedBy { it.date }
        if (sortedPriceList.size > 14) {
            for (i in 14 until sortedPriceList.size) {
                assertNotNull(sortedPriceList[i].rsi, "인덱스 {$i}의 RSI 값이 null입니다")
                assertTrue(sortedPriceList[i].rsi!! in 0f..100f, "RSI 값이 0에서 100 사이가 아닙니다: ${sortedPriceList[i].rsi}")
            }
        }
    }

    @Test
    fun `특정 날짜의 주식 가격 정보를 조회한다`() {
        // Given
        val stockCodeList = stockCodeRepository.findByName("LG화학").map { it.code }
        val targetDate = LocalDate.now().minusDays(5)

        // 데이터 준비
        stockInfoService.upsertStockInfo(stockCodeList)
        stockPriceService.upsertStockPrice(stockCodeList, LocalDate.now(), 10)

        // When
        val stockPriceList = stockPriceService.getStockPriceByDate(targetDate, stockCodeList)

        // Then
        if (stockPriceList.isNotEmpty()) {
            assertEquals(targetDate, stockPriceList[0].date)
            assertEquals(stockCodeList[0], stockPriceList[0].stockInfo.code)

            // 주식 가격 데이터 검증
            assertNotNull(stockPriceList[0].open)
            assertNotNull(stockPriceList[0].high)
            assertNotNull(stockPriceList[0].low)
            assertNotNull(stockPriceList[0].close)
            assertNotNull(stockPriceList[0].volume)
        }
    }
}
