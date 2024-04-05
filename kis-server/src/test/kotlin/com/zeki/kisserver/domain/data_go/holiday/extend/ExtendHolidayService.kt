package com.zeki.kisserver.domain.data_go.holiday.extend


import com.zeki.holiday.DataGoHolidayResDto
import com.zeki.holiday.HolidayJoinRepository
import com.zeki.holiday.HolidayRepository
import com.zeki.kisserver.domain.data_go.holiday.HolidayDateService
import com.zeki.kisserver.domain.data_go.holiday.HolidayService
import com.zeki.kisserver.utils.TestUtils
import com.zeki.webclient.WebClientConnector

class ExtendHolidayService(
    holidayRepository: HolidayRepository,
    holidayJoinRepository: HolidayJoinRepository,
    holidayDateService: HolidayDateService,
    webClientConnector: WebClientConnector,
) : HolidayService(
    holidayRepository = holidayRepository,
    holidayJoinRepository = holidayJoinRepository,
    holidayDateService = holidayDateService,
    webClientConnector = webClientConnector
) {

    override fun getHolidaysFromDataGo(standardYear: Int): DataGoHolidayResDto {
        val filePath2024 = "src/test/resources/holiday/json_response/2024HolidayRes.json"
        val filePath2025 = "src/test/resources/holiday/json_response/2025HolidayRes.json"

        return when (standardYear) {
            2024 -> TestUtils.loadJsonData<DataGoHolidayResDto>(
                filePath2024
            )

            2025 -> TestUtils.loadJsonData<DataGoHolidayResDto>(
                filePath2025
            )

            else -> throw IllegalArgumentException("해당 데이터에 맞는 파일이 없습니다. year: $standardYear")
        }
    }
}