package com.zeki.kisserver.domain.algorithm.elphago

import com.zeki.elphago.Elphago
import com.zeki.elphago.ElphagoRepository
import com.zeki.kisserver.domain.algorithm.algo_base.KisAlgorithm
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AccountDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockBuyDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockSellDto
import com.zeki.kisserver.domain.data_go.stock_code.StockCodeService
import com.zeki.kisserver.domain.kis.stock_price.StockPriceService
import com.zeki.stockdata.stock_price.StockPrice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class ElphagoService(
    private val stockCodeService: StockCodeService,
    private val stockPriceService: StockPriceService,
    private val elphagoRepository: ElphagoRepository
) : KisAlgorithm {

    // 알고리즘에 필요한 상수들 설정
    private val n = 4
    private val n1 = n + 1
    private val delta = BigDecimal("0.99") // BigDecimal로 변경

    // 매수 및 매도 종목을 저장할 맵
    private val buyStocks = mutableMapOf<String, AlgoStockBuyDto>()
    private val sellStocks = mutableMapOf<String, AlgoStockSellDto>()

    @Transactional
    override fun runAlgorithm(baseDate: LocalDate, stockCodeList: List<String>, accountDto: AccountDto) {
        // 필요한 기간의 주가 데이터를 조회

        val elphagoMap = elphagoRepository.findByCodeIn(stockCodeList).associateBy { it.code }

        for (code in stockCodeList) {
            val stockPrices = stockPriceService.getStockPriceListByDate(baseDate, listOf(code))

            val stockName = stockPrices.firstOrNull()?.stockInfo?.name
            val elphago = elphagoMap[code] ?: Elphago(
                code = code,
                name = stockName,
                startDate = stockPrices.firstOrNull()?.date ?: baseDate,
                nowDate = stockPrices.firstOrNull()?.date ?: baseDate
            ).also { elphagoRepository.save(it) }

            val lastProcessedDate = elphago.nowDate
            val startIndex = stockPrices.indexOfFirst { it.date == lastProcessedDate }.takeIf { it >= 0 } ?: 0

            for (i in startIndex until stockPrices.size) {
                val stockPrice = stockPrices[i]

                // 마지막 데이터인 경우 signalFlag 설정
                if (i == stockPrices.size - 1) {
                    elphago.signalFlag = 1
                }

                when (elphago.lineFlag) {
                    -1 -> {
                        // 0파 감지 중
                        if (stockPrice.rsi != null && stockPrice.rsi!! < 30) {
                            elphago.startDate = stockPrice.date
                            elphago.temp1Low = stockPrice.low
                            elphago.temp1High = stockPrice.high
                            elphago.temp1Close = stockPrice.close
                            elphago.lineFlag = 0
                        }
                    }

                    0 -> {
                        // 0~1파 시작
                        if (elphago.temp2High < stockPrice.high) {
                            elphago.temp2Low = stockPrice.low
                            elphago.temp2High = stockPrice.high
                            elphago.temp2Close = stockPrice.close
                        }
                        if (elphago.temp1Low > stockPrice.low) {
                            elphago.temp1Low = stockPrice.low
                            elphago.temp1High = stockPrice.high
                            elphago.temp1Close = stockPrice.close
                            elphago.lineCnt = 0
                        } else {
                            elphago.lineCnt += 1
                            if (elphago.lineCnt == n) {
                                elphago.lineFlag = 1
                                elphago.lineCnt = 0
                            }
                        }
                    }

                    1 -> {
                        // 1~2파 시작
                        if (elphago.temp3Low > stockPrice.low) {
                            elphago.temp3Low = stockPrice.low
                            elphago.temp3High = stockPrice.high
                            elphago.temp3Close = stockPrice.close
                        }
                        if (elphago.temp2Close <= stockPrice.high) {
                            elphago.temp2Low = stockPrice.low
                            elphago.temp2High = stockPrice.high
                            elphago.temp2Close = stockPrice.close
                        } else {
                            elphago.lineCnt += 1
                            if (elphago.lineCnt == n1) {
                                elphago.lineFlag = 2
                                elphago.lineCnt = 0
                            }
                        }
                    }

                    2 -> {
                        // 2~3파 시작
                        if (elphago.temp4High < stockPrice.high) {
                            elphago.temp4Low = stockPrice.low
                            elphago.temp4High = stockPrice.high
                            elphago.temp4Close = stockPrice.close
                        }
                        if (elphago.temp3Low > stockPrice.low) {
                            if (elphago.temp1Low >= stockPrice.low) {
                                // 파쇄 발생, 매도 처리
                                inlinefnSell(elphago, stockPrice)
                            } else {
                                elphago.temp3Low = stockPrice.low
                                elphago.temp3High = stockPrice.high
                                elphago.temp3Close = stockPrice.close
                            }
                        } else {
                            elphago.lineCnt += 1
                            if (elphago.lineCnt == n) {
                                elphago.lineFlag = 3
                                elphago.lineCnt = 0
                                // 매수 신호 발생
                                elphago.buyPrice = stockPrice.close
                                elphago.buyDate = stockPrice.date

                                buyStocks[code] = AlgoStockBuyDto(
                                    stockCode = code,
                                    stockName = stockName ?: "",
                                    buyTargetPrice = stockPrice.close,
                                    detectedDate = stockPrice.date,
                                    buyTargetAmount = BigDecimal.ONE // FIXME : 1말고 의미 있는숫자로 변경
                                )
                            }
                        }
                    }

                    3 -> {
                        // 3~4파 시작
                        if (elphago.temp5Low > stockPrice.low) {
                            elphago.temp5Low = stockPrice.low
                            elphago.temp5High = stockPrice.high
                            elphago.temp5Close = stockPrice.close
                        }
                        if (elphago.temp4Close <= stockPrice.high.multiply(delta)) {
                            elphago.temp4Low = stockPrice.low
                            elphago.temp4High = stockPrice.high
                            elphago.temp4Close = stockPrice.close
                        } else {
                            elphago.lineCnt += 1
                            if (elphago.lineCnt == n) {
                                elphago.lineFlag = 4
                                elphago.lineCnt = 0
                            }
                        }
                    }

                    4 -> {
                        // 4~5파 시작
                        if (elphago.tempaHigh < stockPrice.high) {
                            elphago.tempaLow = stockPrice.low
                            elphago.tempaHigh = stockPrice.high
                            elphago.tempaClose = stockPrice.close
                        }
                        if (elphago.temp5Low > stockPrice.low) {
                            if (elphago.temp1High >= stockPrice.close) {
                                // 파쇄 발생, 매도 처리
                                inlinefnSell(elphago, stockPrice)
                            } else {
                                elphago.temp5Low = stockPrice.low
                                elphago.temp5High = stockPrice.high
                                elphago.temp5Close = stockPrice.close
                            }
                        } else {
                            elphago.lineCnt += 1
                            if (elphago.lineCnt == n) {
                                elphago.lineFlag = 5
                                elphago.lineCnt = 0
                            }
                        }
                    }

                    5 -> {
                        // 5~a파 시작
                        if (elphago.tempaClose <= stockPrice.high.multiply(delta)) {
                            elphago.tempaLow = stockPrice.low
                            elphago.tempaHigh = stockPrice.high
                            elphago.tempaClose = stockPrice.close
                        } else {
                            elphago.lineCnt += 1
                            if (elphago.lineCnt == n1) {
                                // 매도 신호 발생
                                inlinefnSell(elphago, stockPrice)
                            }
                        }
                    }
                }

                // 매수 후 일정 기간 지나면 매도 처리
                if (elphago.buyPrice != BigDecimal.ONE) {
                    elphago.buyDateCnt += 1
                    if (elphago.buyDateCnt > 20) {
                        inlinefnSell(elphago, stockPrice)
                    }
                }

                // 거래량이 0인 경우 처리
                if (stockPrice.volume == 0L) {
                    elphago.volStack += 1
                } else {
                    elphago.volStack = 0
                }

                if (elphago.volStack >= 3) {
                    inlinefnSell(elphago, stockPrice)
                }

                // 현재 날짜 업데이트
                elphago.nowDate = stockPrice.date
            }

            // elphago 엔티티 저장
            elphagoRepository.save(elphago)
        }
    }

    override fun getBuyStocks(baseDate: LocalDate): Map<String, AlgoStockBuyDto> {
        // 해당 기준일의 매수 종목 반환
        return buyStocks.filterValues { it.detectedDate == baseDate }
    }

    override fun getSellStocks(baseDate: LocalDate): Map<String, AlgoStockSellDto> {
        // 해당 기준일의 매도 종목 반환
        return sellStocks.filterValues { it.detectedDate == baseDate }
    }

    private fun inlinefnSell(elphago: Elphago, stockPrice: StockPrice) {
        // 매도 처리 로직 구현
        val sellDto = AlgoStockSellDto(
            stockCode = stockPrice.stockInfo.code,
            stockName = stockPrice.stockInfo.name,
            sellTargetPrice = stockPrice.close,
            detectedDate = stockPrice.date
        )
        sellStocks[elphago.code] = sellDto

        // 변수 초기화
        elphago.lineFlag = -1
        elphago.lineCnt = 0
        elphago.buyPrice = BigDecimal.ONE
        elphago.buyDate = null
        elphago.buyDateCnt = 0
        elphago.volStack = 0
        // temp 변수들 초기화
        elphago.temp1Low = BigDecimal.ZERO
        elphago.temp1High = BigDecimal.ZERO
        elphago.temp1Close = BigDecimal.ZERO
        elphago.temp2Low = BigDecimal.ZERO
        elphago.temp2High = BigDecimal.ZERO
        elphago.temp2Close = BigDecimal.ZERO
        elphago.temp3Low = BigDecimal.ZERO
        elphago.temp3High = BigDecimal.ZERO
        elphago.temp3Close = BigDecimal.ZERO
        elphago.temp4Low = BigDecimal.ZERO
        elphago.temp4High = BigDecimal.ZERO
        elphago.temp4Close = BigDecimal.ZERO
        elphago.temp5Low = BigDecimal.ZERO
        elphago.temp5High = BigDecimal.ZERO
        elphago.temp5Close = BigDecimal.ZERO
        elphago.tempaLow = BigDecimal.ZERO
        elphago.tempaHigh = BigDecimal.ZERO
        elphago.tempaClose = BigDecimal.ZERO
    }
}
