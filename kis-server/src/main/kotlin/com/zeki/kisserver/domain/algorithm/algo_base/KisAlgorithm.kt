package com.zeki.kisserver.domain.algorithm.algo_base

import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockBuyDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockSellDto
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
interface KisAlgorithm {
    fun runAlgorithm(baseDate: LocalDate, stockCodeList: List<String>)

    fun getBuyStocks(baseDate: LocalDate): List<AlgoStockBuyDto>
    fun getSellStocks(baseDate: LocalDate): List<AlgoStockSellDto>
}