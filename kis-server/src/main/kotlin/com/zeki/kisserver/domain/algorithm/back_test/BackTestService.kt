package com.zeki.kisserver.domain.algorithm.back_test

import com.zeki.common.em.OrderType
import com.zeki.common.exception.ExceptionUtils.log
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AccountDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AccountHistoryDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockBuyDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockSellDto
import com.zeki.kisserver.domain.algorithm.elphago.ElphagoService
import com.zeki.kisserver.domain.data_go.holiday.HolidayService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BackTestService(
    private val stockPriceService: StockPriceService,
    private val kisAlgorithm: ElphagoService,
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
            stockInfos = mutableMapOf()
        )
        val accountHistoryDto = AccountHistoryDto()

        val dateList = holidayService.getHolidayList(startDate, endDate)

        for (dateEntity in dateList) {
            if (dateEntity.isHoliday) continue
            val baseDate = dateEntity.date
            kisAlgorithm.runAlgorithm(baseDate, stockCodeList, accountDto)

            val buyStocks = kisAlgorithm.getBuyStocks(dateEntity.date)
            val sellStocks = kisAlgorithm.getSellStocks(dateEntity.date)

            this.buyStocks(baseDate, buyStocks, accountDto)
            this.sellStocks(baseDate, sellStocks, accountDto, accountHistoryDto)
        }

        // TODO : 덽타 값 부여 (실제 수익률은 테스트 결과에 따라 다름)
        // TODO : 반복

        val winRate = accountHistoryDto.winCount / (accountHistoryDto.winCount + accountHistoryDto.loseCount).toFloat()
        val loseRate =
            accountHistoryDto.loseCount / (accountHistoryDto.winCount + accountHistoryDto.loseCount).toFloat()

        val calri = winRate / accountHistoryDto.winProfit - loseRate / accountHistoryDto.loseProfit

        log.info("calri : $calri")
    }

    @Transactional(readOnly = true)
    fun buyStocks(baseDate: LocalDate, buyDtos: Map<String, AlgoStockBuyDto>, accountDto: AccountDto) {
        val stockPriceByDate = stockPriceService.getStockPriceByDate(baseDate, buyDtos.entries.map { it.key })

        for (stockPrice in stockPriceByDate) {
            val stockName = stockPrice.stockInfo.name
            val stockCode = stockPrice.stockInfo.code
            val stockOpenPrice = stockPrice.open
            val stockVolume = stockPrice.volume

            if (stockVolume == 0L) continue

            buyDtos[stockCode]?.let {
                when (val accountStockInfoDto = accountDto.stockInfos[stockCode]) {
                    null -> {
                        val stockInfoDto = AccountDto.StockInfoDto(
                            stockCode = stockCode,
                            stockName = stockName,
                            stockAvgPrice = stockOpenPrice,
                            stockAmount = it.buyTargetAmount,
                            stockTradeInfos = mutableListOf(
                                AccountDto.StockTradeInfoDto(
                                    orderType = OrderType.BUY,
                                    tradeDate = baseDate,
                                    tradePrice = stockOpenPrice,
                                    tradeAmount = it.buyTargetAmount
                                )
                            )
                        )
                        val buyTotalPrice = it.buyTargetPrice * it.buyTargetAmount
                        if (accountDto.totalPrice < buyTotalPrice) return@let

                        accountDto.stockInfos[stockCode] = stockInfoDto
                        accountDto.totalPrice -= buyTotalPrice
                        accountDto.stockPrice += buyTotalPrice
                        accountDto.cashPrice -= buyTotalPrice
                    }

                    else -> {
                        accountStockInfoDto.stockTradeInfos.add(
                            AccountDto.StockTradeInfoDto(
                                orderType = OrderType.BUY,
                                tradeDate = baseDate,
                                tradePrice = stockOpenPrice,
                                tradeAmount = it.buyTargetAmount
                            )
                        )
                        accountStockInfoDto.stockAmount += it.buyTargetAmount
                        accountStockInfoDto.stockAvgPrice =
                            (accountStockInfoDto.stockAvgPrice * (accountStockInfoDto.stockAmount - it.buyTargetAmount) + stockOpenPrice * it.buyTargetAmount) / accountStockInfoDto.stockAmount

                        val buyTotalPrice = it.buyTargetPrice * it.buyTargetAmount
                        accountDto.totalPrice -= buyTotalPrice
                        accountDto.stockPrice += buyTotalPrice
                        accountDto.cashPrice -= buyTotalPrice
                    }
                }
            }

        }
    }

    @Transactional(readOnly = true)
    fun sellStocks(
        baseDate: LocalDate,
        sellDtos: Map<String, AlgoStockSellDto>,
        accountDto: AccountDto,
        accountHistoryDto: AccountHistoryDto
    ) {
        val stockPriceByDate = stockPriceService.getStockPriceByDate(baseDate, sellDtos.entries.map { it.key })

        for (stockPrice in stockPriceByDate) {
            val stockCode = stockPrice.stockInfo.code
            val stockClosePrice = stockPrice.close
            val stockVolume = stockPrice.volume

            if (stockVolume == 0L) continue

            sellDtos[stockCode]?.let {
                val accountStockInfoDto = accountDto.stockInfos[stockCode] ?: return@let

                accountStockInfoDto.stockTradeInfos.add(
                    AccountDto.StockTradeInfoDto(
                        orderType = OrderType.SELL,
                        tradeDate = baseDate,
                        tradePrice = stockClosePrice,
                        tradeAmount = it.sellTargetAmount
                    )
                )
                accountStockInfoDto.stockAmount -= it.sellTargetAmount
                accountStockInfoDto.stockAvgPrice =
                    (accountStockInfoDto.stockAvgPrice * (accountStockInfoDto.stockAmount + it.sellTargetAmount) - stockClosePrice * it.sellTargetAmount) / accountStockInfoDto.stockAmount

                val sellTotalPrice = it.sellTargetPrice * it.sellTargetAmount
                accountDto.totalPrice += sellTotalPrice
                accountDto.stockPrice -= sellTotalPrice
                accountDto.cashPrice += sellTotalPrice

                if (accountStockInfoDto.stockAmount == BigDecimal.ZERO) {
                    val stockInfoDto = accountDto.stockInfos[stockCode] ?: return@let
                    var buyPrice = BigDecimal.ZERO
                    var sellPrice = BigDecimal.ZERO
                    for (stockTradeInfo in stockInfoDto.stockTradeInfos) {
                        when (stockTradeInfo.orderType) {
                            OrderType.BUY -> {
                                buyPrice += stockTradeInfo.tradePrice * stockTradeInfo.tradeAmount
                            }

                            OrderType.SELL -> {
                                sellPrice += stockTradeInfo.tradePrice * stockTradeInfo.tradeAmount
                            }
                        }
                    }
                    val rate = ((sellPrice - buyPrice) / buyPrice).toFloat()
                    if (rate < 0) {
                        accountHistoryDto.loseProfit =
                            ((accountHistoryDto.loseProfit * accountHistoryDto.loseCount) + rate) / (accountHistoryDto.loseCount + 1)
                        accountHistoryDto.loseCount += 1
                    } else {
                        accountHistoryDto.winProfit =
                            ((accountHistoryDto.winProfit * accountHistoryDto.winCount) + rate) / (accountHistoryDto.winCount + 1)
                        accountHistoryDto.winCount += 1
                    }

                    accountDto.stockInfos.remove(stockCode)
                }
            }
        }
    }
}