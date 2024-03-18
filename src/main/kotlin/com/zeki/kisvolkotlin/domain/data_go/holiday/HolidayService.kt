package com.zeki.kisvolkotlin.domain.data_go.holiday

import com.zeki.kisvolkotlin.db.repository.HolidayRepository
import com.zeki.kisvolkotlin.domain.data_go.holiday.dto.HolidayResDto
import com.zeki.kisvolkotlin.exception.ApiException
import com.zeki.kisvolkotlin.exception.ResponseCode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.time.LocalTime

@Service
class HolidayService(
    private val holidayRepository: HolidayRepository,
    @Qualifier("WebClientDataGo") private val webClientDataGo: WebClient,
) {

    @Transactional
    fun getAvailableDate(standardTime: LocalTime = LocalTime.now()): LocalDate {
        var availableDate = when {
            standardTime.isBefore(LocalTime.of(16, 0)) -> LocalDate.now().minusDays(1)
            else -> LocalDate.now()
        }

        while (this.isHoliday(availableDate)) {
            availableDate = availableDate.minusDays(1)
        }

        return availableDate
    }

    fun isHoliday(availableDate: LocalDate): Boolean {
        val holiday = holidayRepository.findByDate(availableDate)

        return when (holiday) {
            null -> false
            else -> true
        }
    }

    @Transactional
    fun upsertHoliday(standardDate: LocalDate = LocalDate.now()) {
        val holidayResDto = this.getHolidaysFromDataGo(standardDate)

        for (item in holidayResDto.response.body.items.item) {

        }

    }


    fun getHolidaysFromDataGo(standardDate: LocalDate = LocalDate.now()): HolidayResDto {
        val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()
        queryParams.add("solYear", standardDate.year.toString())
        queryParams.add("_type", "json")
        queryParams.add("numOfRows", "100")

        val responseDatas = webClientDataGo.get()
            .uri {
                it.path("B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
                    .queryParams(queryParams)
                    .build()
            }
            .exchangeToMono { it.toEntity(HolidayResDto::class.java) }
            .block()

        val holidayResDto = responseDatas?.body ?: HolidayResDto()

        if (holidayResDto.response.header.resultCode != "00") {
            throw ApiException(
                ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR,
                "유효한 결과값이 아닙니다. ${holidayResDto.response.header.resultMsg}"
            )
        }

        return holidayResDto
    }
}