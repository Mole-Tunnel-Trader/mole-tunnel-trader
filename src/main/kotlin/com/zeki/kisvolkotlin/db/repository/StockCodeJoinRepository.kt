package com.zeki.kisvolkotlin.db.repository

import com.zeki.kisvolkotlin.db.entity.StockCode
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class StockCodeJoinRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private val batchSize = 1000

    fun bulkInsert(stockCodeSaveList: Collection<StockCode>) {
        stockCodeSaveList.chunked(batchSize).forEach {
            this.bulkInsertUsingBatch(it)
        }

    }

    private fun bulkInsertUsingBatch(stockCodeSaveList: Collection<StockCode>) {
        var sql = buildString {
            append("INSERT INTO stock_code (code, name, market) VALUES ")

            stockCodeSaveList.forEach {
                append("('${it.code}', '${it.name}', '${it.market}'), ")
            }
        }

        sql = sql.substring(0, sql.length - 2)
        jdbcTemplate.execute(sql)
    }

    fun bulkUpdate(stockCodeUpdateList: Collection<StockCode>) {
        stockCodeUpdateList.chunked(batchSize).forEach {
            this.bulkUpdateUsingBatch(it)
        }
    }

    private fun bulkUpdateUsingBatch(stockCodeUpdateList: Collection<StockCode>) {
        val sql = "UPDATE stock_code SET name = ?, market = ? WHERE id = ?"

        jdbcTemplate.batchUpdate(sql, stockCodeUpdateList.map {
            arrayOf(it.name, it.market.name, it.id)
        })
    }
}
