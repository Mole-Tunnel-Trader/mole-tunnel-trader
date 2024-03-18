package com.zeki.kisvolkotlin.db.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import java.time.LocalDate

@Entity
class Holiday(
    name: String,
    date: LocalDate
) : BaseEntity() {

    @Column(length = 50, nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var date: LocalDate = date
        protected set

    fun updateHoliday(
        name: String,
        date: LocalDate
    ): Boolean {
        if (this.name == name &&
            this.date == date
        ) return false

        this.name = name
        this.date = date
        return true
    }
}