package com.zeki.kisvolkotlin.db.repository

import com.zeki.kisvolkotlin.db.entity.Holiday
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface HolidayRepository : JpaRepository<Holiday, Long> {
    fun findByDate(availableDate: LocalDate): Holiday?
}