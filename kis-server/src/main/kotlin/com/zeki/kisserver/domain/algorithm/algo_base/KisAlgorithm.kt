package com.zeki.kisserver.domain.algorithm.algo_base

import com.zeki.kisserver.domain.algorithm.algo_base.dto.AccountDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockBuyDto
import com.zeki.kisserver.domain.algorithm.algo_base.dto.AlgoStockSellDto
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
interface KisAlgorithm {
    fun runAlgorithm(baseDate: LocalDate, stockCodeList: List<String>, accountDto: AccountDto)

    fun getBuyStocks(baseDate: LocalDate): Map<String, AlgoStockBuyDto>
    fun getSellStocks(baseDate: LocalDate): Map<String, AlgoStockSellDto>
}