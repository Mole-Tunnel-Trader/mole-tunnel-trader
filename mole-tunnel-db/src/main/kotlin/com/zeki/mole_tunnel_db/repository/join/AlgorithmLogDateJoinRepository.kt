package com.zeki.mole_tunnel_db.repository.join

import com.zeki.mole_tunnel_db.entity.AlgorithmLogDate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AlgorithmLogDateJoinRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private val batchSize = 1000

    fun bulkInsert(backTestLogDateSaveList: Collection<AlgorithmLogDate>) {
        backTestLogDateSaveList.chunked(batchSize).forEach {
            this.bulkInsertUsingBatch(it)
        }
    }

    private fun bulkInsertUsingBatch(backTestLogDateSaveList: Collection<AlgorithmLogDate>) {
        var sql = buildString {
            append("INSERT INTO algorithm_log_date (algorithm_log_id, date, deposit_price, valuation_price, before_asset_rate, total_asset_rate) VALUES ")

            repeat(backTestLogDateSaveList.size) {
                append("(?, ?, ?, ?, ?, ?), ")
            }
        }
        sql = sql.substring(0, sql.length - 2)

        jdbcTemplate.update(sql) { ps ->
            var i = 1
            backTestLogDateSaveList.forEach { backTestLogDate ->
                ps.setLong(i++, backTestLogDate.algorithmLog.id!!)
                ps.setDate(i++, java.sql.Date.valueOf(backTestLogDate.date))
                ps.setBigDecimal(i++, backTestLogDate.depositPrice)
                ps.setBigDecimal(i++, backTestLogDate.valuationPrice)
                ps.setFloat(i++, backTestLogDate.beforeAssetRate)
                ps.setFloat(i++, backTestLogDate.totalAssetRate)
            }
        }
    }
}