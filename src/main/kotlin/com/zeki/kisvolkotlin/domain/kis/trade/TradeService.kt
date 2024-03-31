package com.zeki.kisvolkotlin.domain.kis.trade

import com.zeki.kisvolkotlin.db.entity.em.OrderState
import com.zeki.kisvolkotlin.domain.kis.trade.dto.KisOrderStockResDto
import com.zeki.kisvolkotlin.domain.kis.trade.dto.TradeQueueDto
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TradeService(
    private val tradeQueueService: TradeQueueService,
    private val tradeHistoryService: TradeHistoryService,

    private val tradeWebClientService: TradeWebClientService
) {

    @Transactional
    fun orderStock() {
        val tradeQueueDtoList = tradeQueueService.getTradeQueue()

        for (tradeQueueDto in tradeQueueDtoList) {
            val tradeQueueDtoItems = tradeQueueDto.items

            tradeQueueDtoItems.forEach {
                val kisOrderStockResDto =
                    tradeWebClientService.orderStock(it.orderType, it.stockCode, it.orderPrice, it.orderAmount)
                this.checkOrderResponse(kisOrderStockResDto, it, tradeQueueDto.orderBy)
            }
        }

        // FIXME : 주문 성공 및 실패에 따라 삭제할지 말지 결정? 비동기로 작업되어서 가능한가..? DB커넥션 부분은 비동기가 아니게 처리 하도록 변경
        tradeQueueService.removeTradeQueue(tradeQueueDtoList)
    }

    @Async
    fun checkOrderResponse(
        kisOrderStockResDto: KisOrderStockResDto,
        tradeQueueDtoItem: TradeQueueDto.Item,
        orderBy: String
    ) {

        val orderState = when (kisOrderStockResDto.rtCd) {
            // TODO : webHook으로 성공 및 실패 알림
            "0" -> OrderState.SUCCESS
            else -> OrderState.FAIL
        }

        tradeHistoryService.createTradeHistory(kisOrderStockResDto, tradeQueueDtoItem, orderState, orderBy)
    }

}