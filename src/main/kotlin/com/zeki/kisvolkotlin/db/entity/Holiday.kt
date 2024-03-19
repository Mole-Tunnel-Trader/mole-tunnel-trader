package com.zeki.kisvolkotlin.db.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(
    name = "holiday", indexes = [
        Index(name = "idx_holiday_date", columnList = "date")
    ]
)
class Holiday(
    name: String,
    date: LocalDate,
    isHoliday: Boolean
) : BaseEntity() {

    @Column(length = 50, nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var date: LocalDate = date
        protected set

    @Column(nullable = false)
    var isHoliday: Boolean = isHoliday
        protected set

    fun updateHoliday(
        name: String,
        date: LocalDate,
        isHoliday: Boolean
    ): Boolean {
        if (this.name == name &&
            this.date == date &&
            this.isHoliday == isHoliday
        ) return false

        this.name = name
        this.date = date
        this.isHoliday = isHoliday
        return true
    }
}