package com.zeki.trade

import org.springframework.data.jpa.repository.JpaRepository

interface TradeQueueRepository : JpaRepository<TradeQueue, Long>