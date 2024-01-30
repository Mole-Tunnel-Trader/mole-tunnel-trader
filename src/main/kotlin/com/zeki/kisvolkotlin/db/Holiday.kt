package com.zeki.kisvolkotlin.db

import java.time.LocalDate

class Holiday (
    id: HolidayId,
    name: String,
    date: LocalDate
){
    var id: HolidayId = id
        protected set

    var name: String = name
        protected set

    var date: LocalDate = date
        protected set

    fun updateHoliday(
        holiday: Holiday
    ): Boolean {
        return !(this.name == holiday.name &&
                this.date == holiday.date)
    }
}

@JvmInline
value class HolidayId (
    val value: Long
)
