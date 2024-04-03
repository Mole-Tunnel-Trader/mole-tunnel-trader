package com.zeki.kisserver.db.repository

import org.springframework.data.jpa.repository.JpaRepository

interface StockCodeRepository : JpaRepository<com.zeki.kisserver.db.entity.StockCode, Long> {
    fun findByCode(code: String): com.zeki.kisserver.db.entity.StockCode?
}