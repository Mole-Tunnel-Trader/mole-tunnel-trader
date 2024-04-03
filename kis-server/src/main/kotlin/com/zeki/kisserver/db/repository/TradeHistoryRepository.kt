package com.zeki.kisserver.db.repository

import com.zeki.db.entity.TradeHistory
import com.zeki.kisserver.db.entity.TradeHistory
import com.zeki.kisvolkotlin.db.entity.TradeHistory
import org.springframework.data.jpa.repository.JpaRepository

interface TradeHistoryRepository : JpaRepository<TradeHistory, Long>