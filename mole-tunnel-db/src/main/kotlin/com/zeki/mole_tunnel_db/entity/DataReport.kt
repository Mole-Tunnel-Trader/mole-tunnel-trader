package com.zeki.mole_tunnel_db.entity

import com.zeki.common.em.ReportType
import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "data_report")
class DataReport private constructor(
    name: ReportType,
    url: String,
    reportDateTime: LocalDateTime,
    content: String,
) : BaseEntity() {
    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 30)
    var name: ReportType = name

    @Column(name = "url", nullable = false)
    var url: String = url

    @Column(name = "report_datetime", nullable = false)
    var reportDateTime: LocalDateTime = reportDateTime

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false)
    var content: String = content

    companion object {
        fun create(
            name: ReportType,
            url: String,
            reportDateTime: LocalDateTime,
            content: String,
        ): DataReport {
            return DataReport(name, url, reportDateTime, content)
        }
    }
}