package com.zeki.mole_tunnel_db.repository.join

import com.zeki.mole_tunnel_db.entity.AlgorithmLogStock
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AlgorithmLogStockJoinRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private val batchSize = 1000

    fun bulkInsert(backTestLogStockSaveList: Collection<AlgorithmLogStock>) {
        backTestLogStockSaveList.chunked(batchSize).forEach {
            this.bulkInsertUsingBatch(it)
        }
    }

    private fun bulkInsertUsingBatch(backTestLogStockSaveList: Collection<AlgorithmLogStock>) {
        var sql = buildString {
            append("INSERT INTO algorithm_log_stock (algorithm_log_id, date, stock_code, order_type, trade_standard_price, quantity) VALUES ")

            repeat(backTestLogStockSaveList.size) {
                append("(?, ?, ?, ?, ?, ?), ")
            }
        }
        sql = sql.substring(0, sql.length - 2)

        jdbcTemplate.update(sql) { ps ->
            var i = 1
            backTestLogStockSaveList.forEach { backTestLogStock ->
                ps.setLong(i++, backTestLogStock.algorithmLog.id!!)
                ps.setDate(i++, java.sql.Date.valueOf(backTestLogStock.date))
                ps.setString(i++, backTestLogStock.stockCode)
                ps.setString(i++, backTestLogStock.orderType.name)
                ps.setBigDecimal(i++, backTestLogStock.tradeStandardPrice)
                ps.setBigDecimal(i++, backTestLogStock.quantity)
            }
        }
    }
}