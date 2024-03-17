package com.zeki.kisvolkotlin.db

import org.jetbrains.exposed.sql.insertAndGetId
import java.time.LocalDate

class Holiday(
    id: HolidayId,
    name: String,
    date: LocalDate
) {
    var id: HolidayId = id
        private set

    var name: String = name
        private set

    var date: LocalDate = date
        private set

    companion object {

        fun createHoliday(
            name: String,
            date: LocalDate
        ): HolidayId {
            return HolidayEntity.insertAndGetId {
                it[HolidayEntity.name] = name
                it[HolidayEntity.date] = date
            }.value.let(::HolidayId)
        }
    }
}

@JvmInline
value class HolidayId(
    val value: Long
)
