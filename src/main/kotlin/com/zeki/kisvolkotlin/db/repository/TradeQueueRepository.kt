package com.zeki.kisvolkotlin.db.repository

import com.zeki.kisvolkotlin.db.entity.TradeQueue
import org.springframework.data.jpa.repository.JpaRepository

interface TradeQueueRepository : JpaRepository<TradeQueue, Long>