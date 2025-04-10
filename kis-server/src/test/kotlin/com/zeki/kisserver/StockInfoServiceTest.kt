package com.zeki.kisserver

import com.zeki.common.em.Status
import com.zeki.common.em.StockMarket
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import com.zeki.mole_tunnel_db.repository.StockInfoRepository
import com.zeki.stockdata.service.stock_info.StockInfoService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Commit
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Commit
class StockInfoServiceTest {

    @Autowired
    private lateinit var stockCodeRepository: StockCodeRepository

    @Autowired
    private lateinit var stockInfoRepository: StockInfoRepository

    @Autowired
    private lateinit var stockInfoService: StockInfoService

    @Test
    fun `주식 정보를 성공적으로 업데이트한다`() {
        // Given
        val stockCodeList = stockCodeRepository.findByIsAliveAndMarketIn(
            Status.Y,
            listOf(StockMarket.KOSPI)
        ).take(5).map { it.code }

        // When
        val upsertReport = stockInfoService.upsertStockInfo(stockCodeList)

        // Then
        assertNotNull(upsertReport)
        assertTrue(upsertReport.updateCount >= 0)

        // 주식 정보가 실제로 저장되었는지 확인
        val stockInfoList = stockInfoService.getStockInfoList(stockCodeList)
        assertEquals(stockCodeList.size, stockInfoList.size)

        // 각 주식 정보의 필수 필드가 채워져 있는지 확인
        stockInfoList.forEach { stockInfo ->
            assertNotNull(stockInfo.code)
            assertNotNull(stockInfo.name)
            assertTrue(stockInfo.code.isNotEmpty())
            assertTrue(stockInfo.name.isNotEmpty())
        }
    }

    @Test
    fun `삼성전자 주식 정보를 조회하고 검증한다`() {
        // Given
        val stockCodeList = stockCodeRepository.findByName("삼성전자").map { it.code }

        // 데이터 준비
        stockInfoService.upsertStockInfo(stockCodeList)

        // When
        val stockInfoList = stockInfoService.getStockInfoList(stockCodeList)

        // Then
        assertFalse(stockInfoList.isEmpty())
        val samsungInfo = stockInfoList[0]

        // 삼성전자 정보 검증
        assertEquals("삼성전자", samsungInfo.name)
        assertNotNull(samsungInfo.marketCapital)
        assertNotNull(samsungInfo.per)
        assertNotNull(samsungInfo.pbr)
        assertNotNull(samsungInfo.eps)

        // 시가총액은 양수여야 함
        assertTrue(samsungInfo.marketCapital > 0)
    }

    @Test
    @Transactional
    fun `여러 종목의 주식 정보를 한번에 조회한다`() {
        // Given
        val targetCompanies = listOf("삼성전자", "SK하이닉스", "LG화학", "현대차", "NAVER")
        val stockCodes = mutableListOf<String>()

        targetCompanies.forEach { companyName ->
            val codes = stockCodeRepository.findByName(companyName).map { it.code }
            stockCodes.addAll(codes)
        }

        // 데이터 준비
        stockInfoService.upsertStockInfo(stockCodes)

        // When
        val stockInfoList = stockInfoService.getStockInfoList(stockCodes)

        // Then
        assertFalse(stockInfoList.isEmpty())
        assertTrue(stockInfoList.size <= targetCompanies.size) // 일부 회사는 코드가 없을 수 있음

        // 각 회사의 정보가 올바르게 조회되었는지 확인
        val companyNames = stockInfoList.map { it.name }
        targetCompanies.forEach { companyName ->
            if (companyNames.contains(companyName)) {
                val stockInfo = stockInfoList.first { it.name == companyName }
                assertNotNull(stockInfo.code)
                assertNotNull(stockInfo.marketCapital)
                assertNotNull(stockInfo.per)
            }
        }
    }

    @Test
    fun `주식 정보의 PER, PBR, EPS 값이 올바른 범위에 있는지 확인한다`() {
        // Given
        val stockCodeList = stockCodeRepository.findByIsAliveAndMarketIn(
            Status.Y,
            listOf(StockMarket.KOSPI, StockMarket.KOSDAQ)
        ).take(10).map { it.code }

        // 데이터 준비
        stockInfoService.upsertStockInfo(stockCodeList)

        // When
        val stockInfoList = stockInfoService.getStockInfoList(stockCodeList)

        // Then
        assertFalse(stockInfoList.isEmpty())

        // PER, PBR, EPS 값 검증
        stockInfoList.forEach { stockInfo ->
            // PER은 일반적으로 양수이며 극단적인 값이 아니어야 함
            if (stockInfo.per > 0) {
                assertTrue(stockInfo.per < 1000, "${stockInfo.name}의 PER이 비정상적으로 높습니다: ${stockInfo.per}")
            }

            // PBR은 일반적으로 양수여야 함
            if (stockInfo.pbr > 0) {
                assertTrue(stockInfo.pbr < 100, "${stockInfo.name}의 PBR이 비정상적으로 높습니다: ${stockInfo.pbr}")
            }

            // 시가총액은 반드시 양수여야 함
            assertTrue(stockInfo.marketCapital >= 0, "${stockInfo.name}의 시가총액이 음수입니다: ${stockInfo.marketCapital}")
        }
    }
} 