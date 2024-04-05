package com.zeki.stockcode

import org.springframework.data.jpa.repository.JpaRepository

interface StockCodeRepository : JpaRepository<StockCode, Long> {
    fun findByCode(code: String): StockCode?
}