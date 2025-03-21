package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.DataReport
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface DataReportRepository : JpaRepository<DataReport, Long> {
    fun findByReportDateTimeBetween(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): MutableList<DataReport>
}