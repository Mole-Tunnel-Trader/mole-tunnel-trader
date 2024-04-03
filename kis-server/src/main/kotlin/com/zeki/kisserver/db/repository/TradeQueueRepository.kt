package com.zeki.kisserver.db.repository

import com.zeki.db.entity.TradeQueue
import com.zeki.kisserver.db.entity.TradeQueue
import com.zeki.kisvolkotlin.db.entity.TradeQueue
import org.springframework.data.jpa.repository.JpaRepository

interface TradeQueueRepository : JpaRepository<TradeQueue, Long>