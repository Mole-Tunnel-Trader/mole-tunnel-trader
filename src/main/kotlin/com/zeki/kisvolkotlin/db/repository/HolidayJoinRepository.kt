package com.zeki.kisvolkotlin.db.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.zeki.kisvolkotlin.db.entity.Holiday
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class HolidayJoinRepository(
    private val jpaQueryFactory: JPAQueryFactory,
    private val jdbcTemplate: JdbcTemplate
) {
    private val batchSize = 1000

    fun bulkInsert(holidaySaveList: List<Holiday>) {
        holidaySaveList.chunked(batchSize).forEach {
            this.bulkInsertUsingBatch(it)
        }
    }

    private fun bulkInsertUsingBatch(holidaySaveList: List<Holiday>) {
        var sql = "INSERT INTO holiday (date, name, is_holiday) VALUES "

        holidaySaveList.forEach {
            sql += "('${it.date}', '${it.name}', ${it.isHoliday}), "
        }
        sql = sql.substring(0, sql.length - 2)

        jdbcTemplate.execute(sql)
    }

    fun bulkUpdate(holidayUpdateList: List<Holiday>) {
        holidayUpdateList.chunked(batchSize).forEach {
            this.bulkUpdateUsingBatch(it)
        }
    }

    private fun bulkUpdateUsingBatch(holidayUpdateList: List<Holiday>) {
        val sql = "UPDATE holiday SET name = ?, date = ?, is_holiday = ? WHERE id = ?"

        jdbcTemplate.batchUpdate(sql, holidayUpdateList.map {
            arrayOf(it.name, it.date, it.isHoliday, it.id)
        })
    }

}