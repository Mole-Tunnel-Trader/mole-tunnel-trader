package com.zeki.kisvolkotlin.domain.data_go.holiday

import com.zeki.kisvolkotlin.db.entity.Holiday
import com.zeki.kisvolkotlin.db.repository.HolidayJoinRepository
import com.zeki.kisvolkotlin.db.repository.HolidayRepository
import com.zeki.kisvolkotlin.domain._common.util.CustomUtils
import com.zeki.kisvolkotlin.domain.data_go.holiday.extend.ExtendHolidayService
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Suppress("LocalVariableName")
class HolidayServiceTest(
    @Autowired private var holidayService: HolidayService,
    @Autowired private var holidayDateService: HolidayDateService,

    @Autowired private var holidayRepository: HolidayRepository,
    @Autowired private var holidayJoinRepository: HolidayJoinRepository,

    @Qualifier("WebClientDataGo") @Autowired private var webClientDataGo: WebClient
) {

    val extendHolidayService: ExtendHolidayService by lazy {
        ExtendHolidayService(
            holidayRepository = holidayRepository,
            holidayJoinRepository = holidayJoinRepository,
            holidayDateService = holidayDateService,
            webClientDataGo = webClientDataGo
        )
    }

    @Nested
    @DisplayName("성공 테스트")
    inner class SuccessTest {
        @Test
        fun `getAvailableDate() - 유효한 시간, 날짜 반환`() {
            // Given
            val holiday1 = Holiday(
                date = LocalDate.of(2024, 2, 9),
                name = "설날",
                isHoliday = true
            )
            val holiday2 = Holiday(
                date = LocalDate.of(2024, 2, 10),
                name = "설날",
                isHoliday = true
            )
            val holiday3 = Holiday(
                date = LocalDate.of(2024, 2, 11),
                name = "설날",
                isHoliday = true
            )
            val holiday4 = Holiday(
                date = LocalDate.of(2024, 2, 12),
                name = "주말",
                isHoliday = false
            )

            holidayRepository.saveAll(listOf(holiday1, holiday2, holiday3, holiday4))

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
        fun `upsertHoliday() - 중복 실행시 데이터 일관성 검증`() {
            // Given
            val `2024 01 01 중복삽입` = Holiday(
                date = LocalDate.of(2024, 1, 1),
                name = "1월1일",
                isHoliday = true
            )

            val `2024 02 09 설날 이름 변경` = Holiday(
                date = LocalDate.of(2024, 2, 9),
                name = "설날이 아닌 문자",
                isHoliday = true
            )

            val `2024 01 02 휴일이 아닌 날짜` = Holiday(
                date = LocalDate.of(2024, 1, 2),
                name = "휴일 아님",
                isHoliday = true
            )

            holidayRepository.saveAll(
                listOf(
                    `2024 01 01 중복삽입`,
                    `2024 02 09 설날 이름 변경`,
                    `2024 01 02 휴일이 아닌 날짜`
                )
            )


            extendHolidayService.upsertHoliday(2024)
            val allEntity = holidayRepository.findAll()
            val size = allEntity.size

            val `20240101 1월1일 true 1개` = allEntity.stream()
                .filter { it.date == LocalDate.of(2024, 1, 1) }
                .toList()

            val `20240209 설날 true 1개` = allEntity.stream()
                .filter {
                    it.date == LocalDate.of(2024, 2, 9) &&
                            it.name == "설날" && it.isHoliday
                }
                .toList()

            val `20240102 휴일아님 true 0개` = allEntity.stream()
                .filter { it.name == "휴일 아님" }
                .toList()

            // When
            extendHolidayService.upsertHoliday(2024)
            val allEntity2 = holidayRepository.findAll()
            val size2 = allEntity2.size

            // Then
            assertAll(
                { assertEquals(true, size > 0) },
                { assertEquals(true, size2 > 0) },
                { assertEquals(size, size2) },
                { assertEquals(1, `20240101 1월1일 true 1개`.size) },
                { assertEquals(1, `20240209 설날 true 1개`.size) },
                { assertEquals(0, `20240102 휴일아님 true 0개`.size) },
            )

        }
    }

}