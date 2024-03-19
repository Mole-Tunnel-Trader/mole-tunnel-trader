package com.zeki.kisvolkotlin.domain.data_go.holiday

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class HolidayServiceTest(
    @Autowired var holidayService: HolidayService,
) {

    @Test
    fun upsertHoliday() {
        // Given

        // When
        holidayService.upsertHoliday()

        // Then
    }
}