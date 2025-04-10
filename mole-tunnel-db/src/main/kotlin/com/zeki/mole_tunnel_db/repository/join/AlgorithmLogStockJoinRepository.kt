package com.zeki.mole_tunnel_db.repository.join

import com.zeki.mole_tunnel_db.entity.AlgorithmLogStock
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AlgorithmLogStockJoinRepository(private val jdbcTemplate: JdbcTemplate) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val batchSize = 1000

    fun bulkInsert(backTestLogStockSaveList: Collection<AlgorithmLogStock>) {
        if (backTestLogStockSaveList.isEmpty()) {
            logger.warn("저장할 AlgorithmLogStock 데이터가 없습니다.")
            return
        }

        logger.info("AlgorithmLogStock 일괄 저장 시작: ${backTestLogStockSaveList.size}개 데이터")
        backTestLogStockSaveList.chunked(batchSize).forEach { this.bulkInsertUsingBatch(it) }
        logger.info("AlgorithmLogStock 일괄 저장 완료")
    }

    private fun bulkInsertUsingBatch(backTestLogStockSaveList: Collection<AlgorithmLogStock>) {
        var sql = buildString {
            append(
                "INSERT INTO algorithm_log_stock (algorithm_log_id, date, stock_code, order_type, trade_standard_price, quantity) VALUES "
            )

            repeat(backTestLogStockSaveList.size) { append("(?, ?, ?, ?, ?, ?), ") }
        }
        sql = sql.substring(0, sql.length - 2)

        jdbcTemplate.update(sql) { ps ->
            var i = 1
            backTestLogStockSaveList.forEach { backTestLogStock ->
                val algorithmLogId =
                    backTestLogStock.algorithmLog.id
                        ?: throw IllegalStateException(
                            "AlgorithmLog의 ID가 null입니다: ${backTestLogStock}"
                        )

                ps.setLong(i++, algorithmLogId)
                ps.setDate(i++, java.sql.Date.valueOf(backTestLogStock.date))
                ps.setString(i++, backTestLogStock.stockCode)
                ps.setString(i++, backTestLogStock.orderType.name)
                ps.setBigDecimal(i++, backTestLogStock.tradeStandardPrice)
                ps.setBigDecimal(i++, backTestLogStock.quantity)
            }
        }
    }
}
