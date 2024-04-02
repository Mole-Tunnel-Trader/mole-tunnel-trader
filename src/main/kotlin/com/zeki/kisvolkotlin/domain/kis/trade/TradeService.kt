package com.zeki.kisvolkotlin.domain.kis.trade

import com.zeki.kisvolkotlin.db.entity.em.OrderState
import com.zeki.kisvolkotlin.domain.kis.trade.dto.KisOrderStockResDto
import com.zeki.kisvolkotlin.domain.kis.trade.dto.TradeQueueDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TradeService(
    private val tradeQueueService: TradeQueueService,
    private val tradeHistoryService: TradeHistoryService,

    private val tradeWebClientService: TradeWebClientService
) {

    @Transactional
    fun orderStockByTradeQueue() {
        val tradeQueueDtoList = tradeQueueService.getTradeQueue()

        for (tradeQueueDto in tradeQueueDtoList) {
            val tradeQueueDtoItems = tradeQueueDto.items

            tradeQueueDtoItems.forEach {
                val kisOrderStockResDto =
                    tradeWebClientService.orderStock(it.orderType, it.stockCode, it.orderPrice, it.orderAmount)
                this.checkOrderResponse(kisOrderStockResDto, it, tradeQueueDto.orderBy)
            }
        }

        tradeQueueService.removeTradeQueue(
            tradeQueueDtoList.stream()
                .map { tradeQueueDto ->
                    tradeQueueDto.items.stream()
                        .map { item ->
                            item.id
                        }.toList()
                }.toList()
                .flatten()
        )
    }

    fun checkOrderResponse(
        kisOrderStockResDto: KisOrderStockResDto,
        tradeQueueDtoItem: TradeQueueDto.Item,
        orderBy: String
    ) {

        val orderState = when (kisOrderStockResDto.rtCd) {
            "0" -> OrderState.SUCCESS
            else -> OrderState.FAIL
        }

        // TODO : webHook으로 성공 및 실패 알림 (비동기)

        tradeHistoryService.createTradeHistory(kisOrderStockResDto, tradeQueueDtoItem, orderState, orderBy)
    }

}