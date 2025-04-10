package com.zeki.kisserver.domain.kis.trade

import com.zeki.common.em.OrderState
import com.zeki.mole_tunnel_db.dto.KisOrderStockResDto
import com.zeki.mole_tunnel_db.dto.TradeQueueDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TradeService(
    private val tradeQueueService: TradeQueueService,
    private val tradeHistoryService: TradeHistoryService,
    private val tradeConnectService: TradeConnectService
) {

    @Transactional
    fun orderStockByTradeQueue() {
        val tradeQueueDtoList = tradeQueueService.getTradeQueue()

        for (tradeQueueDto in tradeQueueDtoList) {
            val tradeQueueDtoItems = tradeQueueDto.items

            tradeQueueDtoItems.forEach {
                val kisOrderStockResDto =
                    tradeConnectService.orderStock(
                        it.orderType,
                        it.stockCode,
                        it.orderPrice,
                        it.orderAmount,
                        it.account
                    )
                this.checkOrderResponse(kisOrderStockResDto, it, tradeQueueDto.orderBy)
            }
        }

        tradeQueueService.removeTradeQueue(
            tradeQueueDtoList
                .map { tradeQueueDto ->
                    tradeQueueDto.items.map { item -> item.id }.toList()
                }
                .toList()
                .flatten()
        )
    }

    fun checkOrderResponse(
        kisOrderStockResDto: KisOrderStockResDto,
        tradeQueueDtoItem: TradeQueueDto.Item,
        orderBy: String
    ) {

        val orderState =
            when (kisOrderStockResDto.rtCd) {
                "0" -> OrderState.SUCCESS
                else -> OrderState.FAIL
            }

        // TODO : webHook으로 성공 및 실패 알림 (비동기)

        tradeHistoryService.createTradeHistory(
            kisOrderStockResDto,
            tradeQueueDtoItem,
            orderState,
            orderBy
        )
    }
}
