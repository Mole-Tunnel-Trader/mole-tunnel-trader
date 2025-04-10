package com.zeki.stockcode.service

import com.zeki.mole_tunnel_db.entity.StockCode
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetStockCodeService(
    private val stockCodeRepository: StockCodeRepository,
) {

    @Transactional(readOnly = true)
    fun getStockCodeList(): List<StockCode> {
        return stockCodeRepository.findByIsAlive()
    }

    @Transactional(readOnly = true)
    fun getStockCodeStringList(): List<String> {
        return stockCodeRepository.findByIsAlive().map { it.code }
    }
}