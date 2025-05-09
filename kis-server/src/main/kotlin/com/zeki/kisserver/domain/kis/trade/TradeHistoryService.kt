package com.zeki.kisserver.domain.kis.trade

import com.zeki.common.em.OrderState
import com.zeki.mole_tunnel_db.dto.KisOrderStockResDto
import com.zeki.mole_tunnel_db.dto.TradeQueueDto
import com.zeki.mole_tunnel_db.entity.TradeHistory
import com.zeki.mole_tunnel_db.repository.TradeHistoryRepository
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