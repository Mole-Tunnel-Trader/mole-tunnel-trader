package com.zeki.holiday.dto.report

data class UpsertReportDto(
    val newCount: Int,
    val updateCount: Int,
    val deleteCount: Int
)