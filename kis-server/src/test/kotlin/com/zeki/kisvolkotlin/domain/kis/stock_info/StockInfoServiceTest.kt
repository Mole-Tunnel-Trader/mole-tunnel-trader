package com.zeki.kisvolkotlin.domain.kis.stock_info

import com.zeki.kisserver.db.repository.StockInfoJoinRepository
import com.zeki.kisserver.domain._common.webclient.ApiStatics
import com.zeki.kisserver.domain._common.webclient.WebClientConnector
import com.zeki.kisserver.domain.kis.stock_info.StockInfoService
import com.zeki.kisvolkotlin.db.entity.StockInfo
import com.zeki.kisvolkotlin.db.repository.StockInfoJoinRepository
import com.zeki.kisvolkotlin.domain._common.webclient.ApiStatics
import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import com.zeki.kisvolkotlin.domain.kis.stock_info.extend.ExtendStockInfoWebClientService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@Suppress("LocalVariableName")
@Transactional
@ActiveProfiles("test")
@SpringBootTest
class StockInfoServiceTest(
    @Autowired private var stockInfoRepository: com.zeki.kisserver.db.repository.StockInfoRepository,
    @Autowired private var stockInfoJoinRepository: StockInfoJoinRepository,

    @Autowired private var apiStatics: ApiStatics,
    @Autowired private var webClientConnector: WebClientConnector,
) {
    private var extendStockInfoWebClientService: ExtendStockInfoWebClientService = ExtendStockInfoWebClientService(
        apiStatics, webClientConnector
    )
    private var extendStockInfoService: StockInfoService = StockInfoService(
        stockInfoRepository, stockInfoJoinRepository, extendStockInfoWebClientService
    )

    @Nested
    @DisplayName("성공 테스트")
    inner class Success {

        @Test
        fun `upsertStockInfo() - 주식 정보 Upsert`() {
            // given
            StockInfo(
                code = "000020",
                name = "동화약품11",
                otherCode = "000020",
                fcamt = 1000,
                amount = 1000,
                marketCapital = 1000,
                capital = 1000,
                per = 1000.0,
                pbr = 1000.0,
                eps = 1000.0
            ).let { stockInfoRepository.save(it) }

            // when
            val stockCodeList = listOf("000020", "000040")
            extendStockInfoService.upsertStockInfo(stockCodeList)

            val 동화약품 = stockInfoRepository.findByCode("000020")!!
            val KR모터스 = stockInfoRepository.findByCode("000040")!!

            // then
            assertAll(
                { assertEquals("동화약품", 동화약품.name) },
                { assertEquals(9.76, 동화약품.per) },
                { assertEquals(982.00, 동화약품.eps) },
                { assertEquals(0.72, 동화약품.pbr) },
                { assertEquals("KR모터스", KR모터스.name) }
            )
        }

    }
}