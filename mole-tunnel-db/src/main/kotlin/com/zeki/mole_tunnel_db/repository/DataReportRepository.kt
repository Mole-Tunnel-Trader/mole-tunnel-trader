package com.zeki.mole_tunnel_db.repository

import com.zeki.common.em.ReportType
import com.zeki.mole_tunnel_db.entity.DataReport
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface DataReportRepository : JpaRepository<DataReport, Long> {
    fun findByReportDateTimeBetweenAndName(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        name: ReportType
    ): MutableList<DataReport>
}