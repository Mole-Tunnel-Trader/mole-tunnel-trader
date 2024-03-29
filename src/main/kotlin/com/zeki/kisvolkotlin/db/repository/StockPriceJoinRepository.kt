package com.zeki.kisvolkotlin.db.repository

import com.zeki.kisvolkotlin.db.entity.StockPrice
import com.zeki.kisvolkotlin.exception.ApiException
import com.zeki.kisvolkotlin.exception.ResponseCode
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class StockPriceJoinRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private val batchSize = 1000

    fun bulkInsert(stockPriceSaveList: Collection<StockPrice>) {
        stockPriceSaveList.chunked(1000).forEach {
            this.bulkInsertUsingBatch(it)
        }
    }

    private fun bulkInsertUsingBatch(stockPriceSaveList: Collection<StockPrice>) {
        var sql = buildString {
            append("INSERT INTO stock_price (date, open, high, low, close, volume, stock_info_id) VALUES ")

            repeat(stockPriceSaveList.size) {
                append("(?, ?, ?, ?, ?, ?, ?), ")
            }
        }
        sql = sql.substring(0, sql.length - 2)

        jdbcTemplate.update(sql) { ps ->
            var i = 1
            stockPriceSaveList.forEach {
                ps.setDate(i++, java.sql.Date.valueOf(it.date))
                ps.setBigDecimal(i++, it.open)
                ps.setBigDecimal(i++, it.high)
                ps.setBigDecimal(i++, it.low)
                ps.setBigDecimal(i++, it.close)
                ps.setLong(i++, it.volume)
                ps.setLong(
                    i++,
                    it.stockInfo.id ?: throw ApiException(
                        ResponseCode.RESOURCE_NOT_FOUND,
                        "StockInfo의 id가 없습니다. code: ${it.stockInfo.code}, name: ${it.stockInfo.name}"
                    )
                )
            }
        }
    }


    fun bulkUpdate(stockPriceUpdateList: Collection<StockPrice>) {
        stockPriceUpdateList.chunked(batchSize).forEach {
            this.bulkUpdateUsingBatch(it)
        }
    }

    private fun bulkUpdateUsingBatch(stockPriceUpdateList: Collection<StockPrice>) {
        val sql =
            "UPDATE stock_price SET open = ?, high = ?, low = ?, close = ?, volume = ? WHERE id = ?"

        jdbcTemplate.batchUpdate(sql, stockPriceUpdateList.map {
            arrayOf(
                it.open,
                it.high,
                it.low,
                it.close,
                it.volume,
                it.id ?: throw ApiException(
                    ResponseCode.RESOURCE_NOT_FOUND,
                    "StockPrice의 id가 없습니다. date: ${it.date}, stockInfo: ${it.stockInfo.code}"
                )
            )
        })

    }

}
