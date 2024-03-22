package com.zeki.kisvolkotlin.db.repository

import com.zeki.kisvolkotlin.db.entity.Holiday
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

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


            holidaySaveList.forEach {
                append("('${it.date}', '${it.name}', ${it.isHoliday}), ")
            }
        }
        
        sql = sql.substring(0, sql.length - 2)
        jdbcTemplate.execute(sql)
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