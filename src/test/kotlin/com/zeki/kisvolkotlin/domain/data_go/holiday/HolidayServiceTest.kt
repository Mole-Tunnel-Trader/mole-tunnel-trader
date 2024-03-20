package com.zeki.kisvolkotlin.domain.data_go.holiday

import com.zeki.kisvolkotlin.domain._common.util.CustomUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
@Suppress("LocalVariableName")
class HolidayServiceTest(
    @Autowired private var holidayService: HolidayService,
    @Autowired private var holidayDateService: HolidayDateService
) {

    @Nested
    @DisplayName("성공 테스트")
    inner class SuccessTest {
        @Test
        fun `getAvailableDate - 유효한 시간, 날짜 반환`() {
            // Given
            val beforeTime = CustomUtils.getStandardNowDate().minusHours(1L)
            val afterTime = CustomUtils.getStandardNowDate().plusHours(1L)

            /*  2024 02 09 ~ 2024 02 12 (설날)
                2024 02 13 평일 (설날 하루 후) */
            val localDateWeekend = LocalDate.of(2024, 2, 12)
            val localDateNotWeekend = LocalDate.of(2024, 2, 13)

            // When
            val m20240213 = holidayDateService.getAvailableDate(localDateNotWeekend, afterTime)
            val m20240208 = holidayDateService.getAvailableDate(localDateNotWeekend, beforeTime)

            val m20240208_2 = holidayDateService.getAvailableDate(localDateWeekend, afterTime)
            val m20240208_3 = holidayDateService.getAvailableDate(localDateWeekend, beforeTime)

            // Then
            assertEquals(LocalDate.of(2024, 2, 13), m20240213)
            assertEquals(LocalDate.of(2024, 2, 8), m20240208)
            assertEquals(LocalDate.of(2024, 2, 8), m20240208_2)
            assertEquals(LocalDate.of(2024, 2, 8), m20240208_3)
        }

        @Test
        fun `휴일 정보 업데이트 upsertHoliday`() {
            // Given

            // When
            holidayService.upsertHoliday()

            // Then
        }
    }

    @Nested
    @DisplayName("실패 테스트")
    inner class FailTest {
        @Test
        fun ` `() {
            // Given

            // When

            // Then
        }
    }

}