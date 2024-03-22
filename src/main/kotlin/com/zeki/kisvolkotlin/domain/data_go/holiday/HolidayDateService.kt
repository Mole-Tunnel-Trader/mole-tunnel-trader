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
        standardTime: LocalTime = LocalTime.now(),
        standardDeltaDate: Int = 0
    ): LocalDate {

        var availableDate = when {
            standardTime.isBefore(CustomUtils.getStandardNowDate()) -> standardDate.minusDays(1)
            else -> standardDate
        }


        // FIXME : 수학적 계산으로 조금 더 효율적으로 호출하게끔 고려해볼것
        var isHoliday = this.isHoliday(availableDate)

        while (isHoliday) {
            availableDate = availableDate.minusDays(1)
            isHoliday = this.isHoliday(availableDate)
        }

        for (i in 0 until standardDeltaDate) {
            availableDate = availableDate.minusDays(1)
            isHoliday = this.isHoliday(availableDate)

            while (isHoliday) {
                availableDate = availableDate.minusDays(1)
                isHoliday = this.isHoliday(availableDate)
            }
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