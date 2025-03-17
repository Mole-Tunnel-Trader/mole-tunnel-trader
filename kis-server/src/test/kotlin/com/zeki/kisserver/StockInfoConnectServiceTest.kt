package com.zeki.kisserver

import com.zeki.kisserver.domain.kis.stock_info.StockInfoConnectService
import com.zeki.mole_tunnel_db.dto.KisStockInfoResDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StockInfoConnectServiceTest @Autowired constructor(
    private val stockInfoService: StockInfoConnectService
) {


    @Test
    fun testGetKisStockInfoDtoList() {
        // 테스트할 종목 코드 목록
        val stockCodeList = listOf("005930", "000660")

        // 시작일과 종료일 설정
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(1)

        // 결과 리스트 가져오기
        val resultList: List<KisStockInfoResDto> =
            stockInfoService.getKisStockInfoDtoList(stockCodeList, endDate, startDate)

        println("📢 조회 결과:")
        resultList.forEach { stockInfo ->

            // assert를 사용하여 종목명 검증
            when (stockInfo.output1?.stockCode) {
                "005930" -> assertEquals("삼성전자", stockInfo.output1?.stockName, "삼성전자 종목명이 잘못되었습니다.")
                "000660" -> assertEquals("SK하이닉스", stockInfo.output1?.stockName, "SK하이닉스 종목명이 잘못되었습니다.")
                else -> throw IllegalArgumentException("알 수 없는 종목 코드입니다: ${stockInfo.output1?.stockCode}")
            }
        }
    }
}
