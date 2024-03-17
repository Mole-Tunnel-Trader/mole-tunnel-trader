package com.zeki.kisvolkotlin.db

import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDate

object HolidayDao {
    fun createHoliday(
        name: String,
        date: LocalDate
    ): HolidayId {
        return HolidayEntity.insertAndGetId {
            it[HolidayEntity.name] = name
            it[HolidayEntity.date] = date
        }.value.let(::HolidayId)
    }

    fun getHolidayById(id: Long): List<Holiday> {
        return HolidayEntity.select(
            HolidayEntity.id
        )
            .map {
                Holiday(
                    id = HolidayId(it[HolidayEntity.id].value),
                    name = it[HolidayEntity.name],
                    date = it[HolidayEntity.date]
                )
            }
    }

    fun getHolidayByLocalDate(date: LocalDate): List<Holiday> {
        return HolidayEntity.selectAll()
            .where {
                HolidayEntity.date eq date
            }
            .map {
                Holiday(
                    id = HolidayId(it[HolidayEntity.id].value),
                    name = it[HolidayEntity.name],
                    date = it[HolidayEntity.date]
                )
            }
    }
}

