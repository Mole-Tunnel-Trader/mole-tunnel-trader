package com.zeki.common.dto

data class UpsertReportDto(
    val newCount: Int,
    val updateCount: Int,
    val deleteCount: Int
)