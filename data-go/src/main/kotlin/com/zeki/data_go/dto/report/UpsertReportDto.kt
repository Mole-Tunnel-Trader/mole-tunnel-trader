package com.zeki.data_go.dto.report

data class UpsertReportDto(
    val newCount: Int,
    val updateCount: Int,
    val deleteCount: Int
)