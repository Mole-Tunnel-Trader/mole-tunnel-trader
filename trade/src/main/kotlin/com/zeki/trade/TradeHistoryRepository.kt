package com.zeki.trade

import org.springframework.data.jpa.repository.JpaRepository

interface TradeHistoryRepository : JpaRepository<TradeHistory, Long>