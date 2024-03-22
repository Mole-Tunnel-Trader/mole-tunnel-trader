package com.zeki.kisvolkotlin.db.repository

import com.zeki.kisvolkotlin.db.entity.StockCode
import org.springframework.data.jpa.repository.JpaRepository

interface StockCodeRepository : JpaRepository<StockCode, Long> {
    fun findByCode(code: String): StockCode?
}