package com.zeki.kisvolkotlin.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.date

object HolidayEntity : LongIdTable("holiday") {
    val name = varchar("name", 30)
    val date = date("date")
}