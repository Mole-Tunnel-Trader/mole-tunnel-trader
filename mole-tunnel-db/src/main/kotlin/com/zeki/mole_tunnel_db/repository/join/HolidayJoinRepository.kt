package com.zeki.mole_tunnel_db.repository.join

import com.zeki.mole_tunnel_db.entity.Holiday
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Date

@Repository
class HolidayJoinRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    private val batchSize = 1000

    fun bulkInsert(holidaySaveList: Collection<Holiday>) {
        holidaySaveList.chunked(batchSize).forEach {
            this.bulkInsertUsingBatch(it)
        }
    }

    private fun bulkInsertUsingBatch(holidaySaveList: Collection<Holiday>) {
        var sql = buildString {
            append("INSERT INTO holiday (date, name, is_holiday) VALUES ")

            repeat(holidaySaveList.size) {
                append("(?, ?, ?), ")
            }
        }

        sql = sql.substring(0, sql.length - 2)

        jdbcTemplate.update(sql) { ps ->
            var i = 1
            holidaySaveList.forEach { holiday ->
                ps.setDate(i++, Date.valueOf(holiday.date))
                ps.setString(i++, holiday.name)
                ps.setBoolean(i++, holiday.isHoliday)
            }
        }
    }

    fun bulkUpdate(holidayUpdateList: Collection<Holiday>) {
        holidayUpdateList.chunked(batchSize).forEach {
            this.bulkUpdateUsingBatch(it)
        }
    }

    private fun bulkUpdateUsingBatch(holidayUpdateList: Collection<Holiday>) {
        val sql = "UPDATE holiday SET name = ?, date = ?, is_holiday = ? WHERE id = ?"

        jdbcTemplate.batchUpdate(sql, holidayUpdateList.map {
            arrayOf(it.name, it.date, it.isHoliday, it.id)
        })
    }

}