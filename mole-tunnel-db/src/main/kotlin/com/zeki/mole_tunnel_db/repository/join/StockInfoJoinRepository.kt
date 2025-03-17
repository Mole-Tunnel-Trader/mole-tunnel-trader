package com.zeki.mole_tunnel_db.repository.join

import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.mole_tunnel_db.entity.StockInfo
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository


@Repository
class StockInfoJoinRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private val batchSize = 1000

    fun bulkInsert(stockInfoSaveList: Collection<StockInfo>) {
        stockInfoSaveList.chunked(batchSize).forEach {
            bulkInsertUsingBatch(it)
        }
    }

    private fun bulkInsertUsingBatch(stockInfoSaveList: Collection<StockInfo>) {
        val sql = buildString {
            append("INSERT INTO stock_info (name, code, other_code, fcam, amount, market_capital, capital, per, pbr, eps) VALUES ")

            repeat(stockInfoSaveList.size) {
                append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?), ")
            }

            setLength(length - 2) // 마지막 쉼표 제거
            append(" ON DUPLICATE KEY UPDATE ")
            append("name = VALUES(name), ")
            append("other_code = VALUES(other_code), ")
            append("fcam = VALUES(fcam), ")
            append("amount = VALUES(amount), ")
            append("market_capital = VALUES(market_capital), ")
            append("capital = VALUES(capital), ")
            append("per = VALUES(per), ")
            append("pbr = VALUES(pbr), ")
            append("eps = VALUES(eps)")
        }

        jdbcTemplate.update(sql) { ps ->
            var i = 1
            stockInfoSaveList.forEach { stockInfo ->
                ps.setString(i++, stockInfo.name)
                ps.setString(i++, stockInfo.code)
                ps.setString(i++, stockInfo.otherCode)
                ps.setInt(i++, stockInfo.fcam)
                ps.setLong(i++, stockInfo.amount)
                ps.setLong(i++, stockInfo.marketCapital)
                ps.setLong(i++, stockInfo.capital)
                ps.setDouble(i++, stockInfo.per)
                ps.setDouble(i++, stockInfo.pbr)
                ps.setDouble(i++, stockInfo.eps)
            }
        }
    }


    fun bulkUpdate(stockInfoUpdateList: Collection<StockInfo>) {
        stockInfoUpdateList.chunked(batchSize).forEach {
            bulkUpdateUsingBatch(it)
        }
    }

    private fun bulkUpdateUsingBatch(stockInfoUpdateList: Collection<StockInfo>) {
        val sql =
            "UPDATE stock_info SET name = ?, other_code = ?, fcam = ?, amount = ?, market_capital = ?, capital = ?, per = ?, pbr = ?, eps = ? WHERE id = ?"

        jdbcTemplate.batchUpdate(sql, stockInfoUpdateList.map {
            arrayOf(
                it.name,
                it.otherCode,
                it.fcam,
                it.amount,
                it.marketCapital,
                it.capital,
                it.per,
                it.pbr,
                it.eps,
                it.id ?: throw ApiException(
                    ResponseCode.RESOURCE_NOT_FOUND,
                    "StockInfo의 id가 없습니다. code: ${it.code}, name: ${it.name}"
                )
            )
        })
    }

}
