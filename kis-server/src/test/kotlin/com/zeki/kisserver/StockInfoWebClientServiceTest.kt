package com.zeki.kisserver

import com.zeki.kisserver.domain.kis.stock_info.StockInfoWebClientService
import com.zeki.mole_tunnel_db.dto.KisStockInfoResDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@SpringBootTest
@ExtendWith(SpringExtension::class)
class StockInfoWebClientServiceTest @Autowired constructor(
    private val stockInfoService: StockInfoWebClientService
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
            // 각 종목의 이름과 메시지를 출력
            println("📌 종목 코드: ${stockInfo.output1?.stockName}, 메시지: ${stockInfo.msg1}")

            // assert를 사용하여 종목명 검증
            when (stockInfo.output1?.stockCode) {
                "005930" -> assertEquals("삼성전자", stockInfo.output1?.stockName, "삼성전자 종목명이 잘못되었습니다.")
                "000660" -> assertEquals("SK하이닉스", stockInfo.output1?.stockName, "SK하이닉스 종목명이 잘못되었습니다.")
                else -> throw IllegalArgumentException("알 수 없는 종목 코드입니다: ${stockInfo.output1?.stockCode}")
            }
        }
    }
}
