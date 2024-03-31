package com.zeki.kisvolkotlin.domain.kis.trade

import com.zeki.kisvolkotlin.db.entity.TradeHistory
import com.zeki.kisvolkotlin.db.entity.em.OrderState
import com.zeki.kisvolkotlin.db.repository.TradeHistoryRepository
import com.zeki.kisvolkotlin.domain.kis.trade.dto.KisOrderStockResDto
import com.zeki.kisvolkotlin.domain.kis.trade.dto.TradeQueueDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class TradeHistoryService(
    private val tradeHistoryRepository: TradeHistoryRepository
) {

    @Transactional
    fun createTradeHistory(
        kisOrderStockResDto: KisOrderStockResDto,
        tradeQueueDtoItem: TradeQueueDto.Item,
        orderState: OrderState,
        orderBy: String
    ) {
        val tradeHistory = TradeHistory(
            stockCode = tradeQueueDtoItem.stockCode,
            orderDate = LocalDate.now(),
            kisOrderNum = kisOrderStockResDto.output.oDNO,
            krxOrderNum = kisOrderStockResDto.output.kRXFWDGORDORGNO,
            kisOrderTime = kisOrderStockResDto.output.oRDTMD,
            orderBy = orderBy,
            orderState = orderState,
            orderType = tradeQueueDtoItem.orderType,
            orderPrice = tradeQueueDtoItem.orderPrice,
            orderAmount = tradeQueueDtoItem.orderAmount,
            resultCode = kisOrderStockResDto.rtCd,
            messageCode = kisOrderStockResDto.msgCd,
            message = kisOrderStockResDto.msg1,
        )

        tradeHistoryRepository.save(tradeHistory)
    }
}