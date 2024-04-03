package com.zeki.kisvolkotlin.domain.data_go.holiday.extend

import com.zeki.kisvolkotlin.db.repository.HolidayRepository
import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import com.zeki.kisvolkotlin.domain.data_go.holiday.HolidayDateService
import com.zeki.kisvolkotlin.domain.data_go.holiday.HolidayService
import com.zeki.kisvolkotlin.utils.TestUtils

class ExtendHolidayService(
    holidayRepository: HolidayRepository,
    holidayJoinRepository: com.zeki.kisserver.db.repository.HolidayJoinRepository,
    holidayDateService: HolidayDateService,
    webClientConnector: WebClientConnector,
) : HolidayService(
    holidayRepository = holidayRepository,
    holidayJoinRepository = holidayJoinRepository,
    holidayDateService = holidayDateService,
    webClientConnector = webClientConnector
) {

    override fun getHolidaysFromDataGo(standardYear: Int): com.zeki.kisserver.domain.data_go.holiday.dto.DataGoHolidayResDto {
        val filePath2024 = "src/test/resources/holiday/json_response/2024HolidayRes.json"
        val filePath2025 = "src/test/resources/holiday/json_response/2025HolidayRes.json"

        return when (standardYear) {
            2024 -> TestUtils.loadJsonData<com.zeki.kisserver.domain.data_go.holiday.dto.DataGoHolidayResDto>(
                filePath2024
            )

            2025 -> TestUtils.loadJsonData<com.zeki.kisserver.domain.data_go.holiday.dto.DataGoHolidayResDto>(
                filePath2025
            )

            else -> throw IllegalArgumentException("해당 데이터에 맞는 파일이 없습니다. year: $standardYear")
        }
    }
}