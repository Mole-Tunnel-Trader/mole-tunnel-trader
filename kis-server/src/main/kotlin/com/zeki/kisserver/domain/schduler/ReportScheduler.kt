package com.zeki.kisserver.domain.schduler

import com.zeki.report.DataReportService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * DEBUG 로그 표시하지 않도록 분리
 */
@Component
class ReportScheduler(
    private val dataReportService: DataReportService,
) {

    @Scheduled(cron = "0 * * * * *")
    fun sendReportWebhook() {
        val now = LocalDateTime.now()
        dataReportService.sendDataReport(now)
    }

}