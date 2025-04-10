package com.zeki.mole_tunnel_db.repository.join

import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.mole_tunnel_db.entity.StockPrice
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class StockPriceJoinRepository(private val jdbcTemplate: JdbcTemplate) {
    private val batchSize = 1000

    fun bulkInsert(stockPriceSaveList: Collection<StockPrice>) {
        stockPriceSaveList.chunked(1000).forEach { this.bulkInsertUsingBatch(it) }
    }

    private fun bulkInsertUsingBatch(stockPriceSaveList: Collection<StockPrice>) {
        var sql = buildString {
            append(
                "INSERT INTO stock_price (date, open, high, low, close, volume, stock_info_id) VALUES "
            )

            repeat(stockPriceSaveList.size) { append("(?, ?, ?, ?, ?, ?, ?), ") }

            // 마지막 콤마 제거
            deleteRange(length - 2, length)

            // 중복 키가 있을 경우 UPDATE로 처리
            append(" ON DUPLICATE KEY UPDATE ")
            append("open = VALUES(open), ")
            append("high = VALUES(high), ")
            append("low = VALUES(low), ")
            append("close = VALUES(close), ")
            append("volume = VALUES(volume)")
        }

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
                    it.stockInfo.id
                        ?: throw ApiException(
                            ResponseCode.RESOURCE_NOT_FOUND,
                            "StockInfo의 id가 없습니다. code: ${it.stockInfo.code}, name: ${it.stockInfo.name}"
                        )
                )
            }
        }
    }

    fun bulkUpdate(stockPriceUpdateList: Collection<StockPrice>) {
        stockPriceUpdateList.chunked(batchSize).forEach { this.bulkUpdateUsingBatch(it) }
    }

    private fun bulkUpdateUsingBatch(stockPriceUpdateList: Collection<StockPrice>) {
        val sql =
            "UPDATE stock_price SET open = ?, high = ?, low = ?, close = ?, volume = ? WHERE id = ?"

        jdbcTemplate.batchUpdate(
            sql,
            stockPriceUpdateList.map {
                arrayOf(
                    it.open,
                    it.high,
                    it.low,
                    it.close,
                    it.volume,
                    it.id
                        ?: throw ApiException(
                            ResponseCode.RESOURCE_NOT_FOUND,
                            "StockPrice의 id가 없습니다. date: ${it.date}, stockInfo: ${it.stockInfo.code}"
                        )
                )
            }
        )
    }

    /** 볼륨 지표 및 RSI를 대량으로 업데이트하는 함수 성능을 위해 직접 JDBC를 사용 */
    fun bulkUpdateIndicators(stockPriceUpdateList: Collection<StockPrice>) {
        if (stockPriceUpdateList.isEmpty()) return

        stockPriceUpdateList.chunked(batchSize).forEach { this.bulkUpdateIndicatorsUsingBatch(it) }
    }

    private fun bulkUpdateIndicatorsUsingBatch(stockPriceUpdateList: Collection<StockPrice>) {
        val sql =
            """
            UPDATE stock_price 
            SET volume_avg_5 = ?, 
                volume_avg_20 = ?, 
                volume_ratio = ?,
                price_change_rate = ?,
                volatility = ?,
                rsi = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.batchUpdate(
            sql,
            stockPriceUpdateList.map {
                arrayOf(
                    it.volumeAvg5,
                    it.volumeAvg20,
                    it.volumeRatio,
                    it.priceChangeRate,
                    it.volatility,
                    it.rsi,
                    it.id
                        ?: throw ApiException(
                            ResponseCode.RESOURCE_NOT_FOUND,
                            "StockPrice의 id가 없습니다. date: ${it.date}, stockInfo: ${it.stockInfo.code}"
                        )
                )
            }
        )
    }

    /** 주가 기본 정보와 모든 지표 데이터를 한 번에 업데이트하는 함수 */
    fun bulkUpdateAll(stockPriceUpdateList: Collection<StockPrice>) {
        if (stockPriceUpdateList.isEmpty()) return

        stockPriceUpdateList.chunked(batchSize).forEach { this.bulkUpdateAllUsingBatch(it) }
    }

    private fun bulkUpdateAllUsingBatch(stockPriceUpdateList: Collection<StockPrice>) {
        val sql =
            """
            UPDATE stock_price 
            SET open = ?, 
                high = ?, 
                low = ?, 
                close = ?, 
                volume = ?,
                volume_avg_5 = ?, 
                volume_avg_20 = ?, 
                volume_ratio = ?,
                price_change_rate = ?,
                volatility = ?,
                rsi = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.batchUpdate(
            sql,
            stockPriceUpdateList.map {
                arrayOf(
                    it.open,
                    it.high,
                    it.low,
                    it.close,
                    it.volume,
                    it.volumeAvg5,
                    it.volumeAvg20,
                    it.volumeRatio,
                    it.priceChangeRate,
                    it.volatility,
                    it.rsi,
                    it.id
                        ?: throw ApiException(
                            ResponseCode.RESOURCE_NOT_FOUND,
                            "StockPrice의 id가 없습니다. date: ${it.date}, stockInfo: ${it.stockInfo.code}"
                        )
                )
            }
        )
    }

    /** 볼륨 지표만 직접 업데이트하는 함수 - 값만 전달받아 SQL 직접 실행 */
    fun bulkUpdateVolumeIndicatorsByValues(updates: Collection<StockPrice>): Int {
        if (updates.isEmpty()) return 0

        val sql =
            """
            UPDATE stock_price 
            SET volume_avg_5 = ?, 
                volume_avg_20 = ?, 
                volume_ratio = ?,
                price_change_rate = ?,
                volatility = ?
            WHERE id = ?
        """.trimIndent()

        var totalUpdated = 0

        updates.chunked(batchSize).forEach { chunk ->
            val batchArgs =
                chunk.map { data ->
                    arrayOf(
                        data.volumeAvg5,
                        data.volumeAvg20,
                        data.volumeRatio,
                        data.priceChangeRate,
                        data.volatility,
                        data.id
                            ?: throw ApiException(
                                ResponseCode.RESOURCE_NOT_FOUND,
                                "stockprice id 조회 실패"
                            ),
                    )
                }

            val updateCount = jdbcTemplate.batchUpdate(sql, batchArgs).sum()
            totalUpdated += updateCount
        }

        return totalUpdated
    }

    /** RSI만 직접 업데이트하는 함수 - 값만 전달받아 SQL 직접 실행 */
    fun bulkUpdateRsiByValues(updates: Collection<StockPrice>): Int {
        if (updates.isEmpty()) return 0

        val sql = "UPDATE stock_price SET rsi = ? WHERE id = ?"

        var totalUpdated = 0

        updates.chunked(batchSize).forEach { chunk ->
            val batchArgs =
                chunk.map { data ->
                    arrayOf(
                        data.rsi,
                        data.id
                            ?: throw ApiException(
                                ResponseCode.RESOURCE_NOT_FOUND,
                                "stockprice id 조회 실패"
                            )
                    )
                }

            val updateCount = jdbcTemplate.batchUpdate(sql, batchArgs).sum()
            totalUpdated += updateCount
        }

        return totalUpdated
    }
}
