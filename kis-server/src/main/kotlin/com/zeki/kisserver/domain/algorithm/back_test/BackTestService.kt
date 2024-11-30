package com.zeki.kisserver.domain.algorithm.back_test

import com.zeki.kisserver.domain.algorithm.algo_base.KisAlgorithm
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AccountDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockBuyDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockSellDto
import com.zeki.kisserver.domain.data_go.holiday.HolidayService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BackTestService(
    private val stockPriceService: StockPriceService,
    private val kisAlgorithm: KisAlgorithm,
    private val holidayService: HolidayService
) {

    @Transactional
    fun backTest(
        startDate: LocalDate,
        endDate: LocalDate,
        stockCodeList: List<String>
    ) {
        val accountDto = AccountDto(
            totalPrice = BigDecimal(10_000_000),
            stockPrice = BigDecimal.ZERO,
            cashPrice = BigDecimal(10_000_000),
            stockInfos = mutableListOf()
        )

        val holidayList = holidayService.getHolidayList(startDate, endDate)

        for (dateEntity in holidayList) {
            if (dateEntity.isHoliday) continue
            val baseDate = dateEntity.date
            kisAlgorithm.runAlgorithm(baseDate, stockCodeList)

            val buyStocks = kisAlgorithm.getBuyStocks(dateEntity.date)
            val sellStocks = kisAlgorithm.getSellStocks(dateEntity.date)

            this.buyStocks(baseDate, buyStocks, accountDto)
            this.sellStocks(baseDate, sellStocks, accountDto)
        }

        // TODO : 해당 날에 맞는 가격정보 조회 & 알고리즘 조회 (덽타 값 부여)
        // TODO : 비교 후 수익률 누적 (승률, 수익률, 수익금)
        // TODO : 반복

        // TODO : 종료 시 캘리공식 적용 및 결과 출력
    }

    @Transactional(readOnly = true)
    fun buyStocks(baseDate: LocalDate, buyDtos: List<AlgoStockBuyDto>, accountDto: AccountDto) {
        val stockPriceByDate = stockPriceService.getStockPriceByDate(baseDate, buyDtos.map { it.stockCode })

    }

    fun sellStocks(baseDate: LocalDate, sellDtos: List<AlgoStockSellDto>, accountDto: AccountDto) {
        val stockPriceByDate = stockPriceService.getStockPriceByDate(baseDate, sellDtos.map { it.stockCode })

    }
}