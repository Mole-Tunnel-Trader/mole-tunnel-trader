package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.TradeQueue
import org.springframework.data.jpa.repository.JpaRepository

interface TradeQueueRepository : JpaRepository<TradeQueue, Long>