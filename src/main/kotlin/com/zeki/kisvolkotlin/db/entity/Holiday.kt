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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Holiday) return false

        if (name != other.name) return false
        if (date != other.date) return false
        if (isHoliday != other.isHoliday) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + isHoliday.hashCode()
        return result
    }


}