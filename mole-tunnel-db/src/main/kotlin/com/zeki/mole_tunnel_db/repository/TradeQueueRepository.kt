package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.TradeQueue
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository

interface TradeQueueRepository : JpaRepository<TradeQueue, Long> {
    // 주문일자별 조회
    fun findByOrderDate(orderDate: LocalDate): List<TradeQueue>
}
