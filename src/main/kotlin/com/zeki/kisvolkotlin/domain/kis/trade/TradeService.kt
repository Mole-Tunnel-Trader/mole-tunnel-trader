package com.zeki.kisvolkotlin.domain.kis.trade

import com.zeki.kisvolkotlin.domain.kis.trade.dto.KisOrderStockResDto
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class TradeService(
    private val tradeQueueService: TradeQueueService,
    private val tradeHistoryService: TradeHistoryService,

    private val tradeWebClientService: TradeWebClientService
) {

    fun orderStock() {
        val tradeQueueList = tradeQueueService.getTradeQueue()

        tradeQueueList.forEach {
//            val kisOrderStockResDto = tradeWebClientService.orderStock(orderType, stockCode, orderPrice, orderAmount)
//            this.checkOrderResponse(kisOrderStockResDto)
        }
        
    }

    @Async
    fun checkOrderResponse(kisOrderStockResDto: KisOrderStockResDto) {

        when (kisOrderStockResDto.rtCd) {
            "0" -> {
                // 성공
            }

            else -> {
                // 실패
            }
        }

        // TODO : webHook으로 성공 및 실패 알림 & log 저장
    }
}