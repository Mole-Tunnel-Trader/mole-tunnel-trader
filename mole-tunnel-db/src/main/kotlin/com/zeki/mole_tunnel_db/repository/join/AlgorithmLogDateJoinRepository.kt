package com.zeki.mole_tunnel_db.repository.join

import com.zeki.mole_tunnel_db.entity.AlgorithmLogDate
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AlgorithmLogDateJoinRepository(private val jdbcTemplate: JdbcTemplate) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val batchSize = 1000

    fun bulkInsert(backTestLogDateSaveList: Collection<AlgorithmLogDate>) {
        if (backTestLogDateSaveList.isEmpty()) {
            logger.warn("저장할 AlgorithmLogDate 데이터가 없습니다.")
            return
        }

        logger.info("AlgorithmLogDate 일괄 저장 시작: ${backTestLogDateSaveList.size}개 데이터")
        backTestLogDateSaveList.chunked(batchSize).forEach { this.bulkInsertUsingBatch(it) }
        logger.info("AlgorithmLogDate 일괄 저장 완료")
    }

    private fun bulkInsertUsingBatch(backTestLogDateSaveList: Collection<AlgorithmLogDate>) {
        var sql = buildString {
            append(
                "INSERT INTO algorithm_log_date (algorithm_log_id, date, deposit_price, valuation_price, before_asset_rate, total_asset_rate) VALUES "
            )

            repeat(backTestLogDateSaveList.size) { append("(?, ?, ?, ?, ?, ?), ") }
        }
        sql = sql.substring(0, sql.length - 2)

        jdbcTemplate.update(sql) { ps ->
            var i = 1
            backTestLogDateSaveList.forEach { backTestLogDate ->
                val algorithmLogId =
                    backTestLogDate.algorithmLog.id
                        ?: throw IllegalStateException(
                            "AlgorithmLog의 ID가 null입니다: ${backTestLogDate}"
                        )

                ps.setLong(i++, algorithmLogId)
                ps.setDate(i++, java.sql.Date.valueOf(backTestLogDate.date))
                ps.setBigDecimal(i++, backTestLogDate.depositPrice)
                ps.setBigDecimal(i++, backTestLogDate.valuationPrice)
                ps.setFloat(i++, backTestLogDate.beforeAssetRate)
                ps.setFloat(i++, backTestLogDate.totalAssetRate)
            }
        }
    }
}
