package com.zeki.kisvolkotlin.domain.data_go.holiday

import com.zeki.kisvolkotlin.db.repository.HolidayRepository
import com.zeki.kisvolkotlin.domain._common.util.CustomUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@Service
class HolidayDateService(
    private val holidayRepository: HolidayRepository
) {

    @Transactional(readOnly = true)
    // TODO : cache 처리
    fun getAvailableDate(
        standardDate: LocalDate = LocalDate.now(),
        standardTime: LocalTime = LocalTime.now()
    ): LocalDate {
        var availableDate = when {
            standardTime.isBefore(CustomUtils.getStandardNowDate()) -> standardDate.minusDays(1)
            else -> standardDate
        }

        while (this.isHoliday(availableDate)) {
            availableDate = availableDate.minusDays(1)
        }

        return availableDate
    }

    fun isHoliday(availableDate: LocalDate): Boolean {
        val holiday = holidayRepository.findFirstByDate(availableDate)

        return when (holiday) {
            null -> false
            else -> true
        }
    }
}