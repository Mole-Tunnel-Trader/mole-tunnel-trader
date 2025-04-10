package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.TradeQueue
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface TradeQueueRepository : JpaRepository<TradeQueue, Long> {
    // 주문일자별 조회
    fun findByOrderDate(orderDate: LocalDate): List<TradeQueue>
}
