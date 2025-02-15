package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.Holiday
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate


interface HolidayRepository : JpaRepository<Holiday, Long> {
    fun findFirstByDate(availableDate: LocalDate): Holiday?

    @Suppress("SpringDataMethodInconsistencyInspection") // IsHoliday를 Holiday로 인식함
    fun findByDateBetweenAndIsHoliday(startDate: LocalDate, endDate: LocalDate, isHoliday: Boolean): List<Holiday>
    fun findByDateBetweenOrderByDateAsc(startDate: LocalDate, endDate: LocalDate): List<Holiday>
}