package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.TradeHistory
import org.springframework.data.jpa.repository.JpaRepository

interface TradeHistoryRepository : JpaRepository<TradeHistory, Long>