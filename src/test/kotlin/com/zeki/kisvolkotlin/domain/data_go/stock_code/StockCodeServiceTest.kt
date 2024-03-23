package com.zeki.kisvolkotlin.domain.data_go.stock_code

import com.zeki.kisvolkotlin.db.entity.StockCode
import com.zeki.kisvolkotlin.db.entity.em.StockMarket
import com.zeki.kisvolkotlin.db.repository.StockCodeJoinRepository
import com.zeki.kisvolkotlin.db.repository.StockCodeRepository
import com.zeki.kisvolkotlin.domain._common.util.CustomUtils
import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import com.zeki.kisvolkotlin.domain.data_go.holiday.HolidayDateService
import com.zeki.kisvolkotlin.domain.data_go.stock_code.extend.ExtendStockCodeService
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
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockCodeServiceTest(
    @Autowired private var stockCodeService: StockCodeService,
    @Autowired private var stockCodeRepository: StockCodeRepository,
    @Autowired private var stockCodeJoinRepository: StockCodeJoinRepository,
    @Autowired private var holidayDateService: HolidayDateService,
    private var webClientConnector: WebClientConnector
) {

    val extendStockCodeService: ExtendStockCodeService by lazy {
        ExtendStockCodeService(
            stockCodeRepository = stockCodeRepository,
            stockCodeJoinRepository = stockCodeJoinRepository,
            holidayDateService = holidayDateService,
            webClientConnector = webClientConnector
        )
    }

    @Nested
    @DisplayName("성공 테스트")
    inner class SuccessTest {

        @Test
        fun `getStockCodesFromDataGo() - 임시`() {
            // Given
            val `삭제될 종목` = StockCode(
                code = "000000",
                name = "삭제될 종목",
                market = StockMarket.KOSPI
            )

            val `시장 변경` = StockCode(
                code = "000020",
                name = "동화약품",
                market = StockMarket.KOSDAQ
            )

            val `변경된 종목명` = StockCode(
                code = "000040",
                name = "KR모터스 테스트",
                market = StockMarket.KOSPI
            )

            stockCodeRepository.saveAll(
                listOf(`삭제될 종목`, `시장 변경`, `변경된 종목명`)
            )

            val standardDate = LocalDate.of(2024, 3, 4)
            val standardTime = CustomUtils.getStandardNowDate().plusHours(1)
            val deltaDate = 5
            // When
            extendStockCodeService.upsertStockCode(
                standardDate = standardDate,
                standardTime = standardTime,
                standardDeltaDate = deltaDate
            )

            // Then
            assertAll(
                { assertEquals(null, stockCodeRepository.findByCode("000000")) },
                { assertEquals(StockMarket.KOSPI, stockCodeRepository.findByCode("000020")?.market) },
                { assertEquals("KR모터스", stockCodeRepository.findByCode("000040")?.name) },
            )

        }
    }

}
