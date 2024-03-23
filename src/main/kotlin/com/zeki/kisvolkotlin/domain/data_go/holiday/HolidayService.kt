package com.zeki.kisvolkotlin.domain.data_go.holiday

import com.zeki.kisvolkotlin.db.entity.Holiday
import com.zeki.kisvolkotlin.db.repository.HolidayJoinRepository
import com.zeki.kisvolkotlin.db.repository.HolidayRepository
import com.zeki.kisvolkotlin.domain._common.util.CustomUtils.toLocalDate
import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import com.zeki.kisvolkotlin.domain.data_go.holiday.dto.DataGoHolidayResDto
import com.zeki.kisvolkotlin.exception.ApiException
import com.zeki.kisvolkotlin.exception.ResponseCode
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.LocalDate

@Service
class HolidayService(
    private val holidayRepository: HolidayRepository,
    private val holidayJoinRepository: HolidayJoinRepository,

    private val holidayDateService: HolidayDateService,

    private val webClientConnector: WebClientConnector,
) {

    @Transactional
    fun upsertHoliday(standardYear: Int = holidayDateService.getAvailableDate().year) {
        val holidaySaveList = mutableListOf<Holiday>()
        val holidayUpdateList = mutableListOf<Holiday>()
        val holidayDeleteSet = mutableSetOf<Holiday>()

        this.upsertHolidayByDataGo(standardYear, holidaySaveList, holidayUpdateList, holidayDeleteSet)
        this.upsertHolidayByDataGo(standardYear + 1, holidaySaveList, holidayUpdateList, holidayDeleteSet)

        this.upsertHolidayByWeekend(standardYear, holidaySaveList, holidayDeleteSet)
        this.upsertHolidayByWeekend(standardYear + 1, holidaySaveList, holidayDeleteSet)


        holidayJoinRepository.bulkInsert(holidaySaveList)
        holidayJoinRepository.bulkUpdate(holidayUpdateList)
        holidayRepository.deleteAllInBatch(holidayDeleteSet)
    }

    fun getHolidaysFromDataGo(standardYear: Int = holidayDateService.getAvailableDate().year)
            : DataGoHolidayResDto {
        val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()
        queryParams.add("solYear", standardYear.toString())
        queryParams.add("_type", "json")
        queryParams.add("numOfRows", "100")

        val responseDatas = webClientConnector.connect<Unit, DataGoHolidayResDto>(
            WebClientConnector.WebClientType.DATA_GO,
            HttpMethod.GET,
            "B090041/openapi/service/SpcdeInfoService/getRestDeInfo",
            requestParams = queryParams,
            responseClassType = DataGoHolidayResDto::class.java,
            retryCount = 3,
            retryDelay = 510
        )

        val dataGoHolidayResDto = responseDatas?.body ?: DataGoHolidayResDto()

        if (dataGoHolidayResDto.response.header.resultCode != "00") {
            throw ApiException(
                ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR,
                "유효한 결과값이 아닙니다. ${dataGoHolidayResDto.response.header.resultMsg}"
            )
        }

        return dataGoHolidayResDto
    }

    fun upsertHolidayByDataGo(
        standardYear: Int,
        holidaySaveList: MutableCollection<Holiday>,
        holidayUpdateList: MutableCollection<Holiday>,
        holidayDeleteSet: MutableCollection<Holiday>
    ) {
        val dataGoHolidayResDto = this.getHolidaysFromDataGo(standardYear)
        val targetHolidayDataList = dataGoHolidayResDto.response.body.items.item

        val startDate = LocalDate.of(standardYear, 1, 1)
        val endDate = LocalDate.of(standardYear, 12, 31)

        val savedHolidayMap = holidayRepository.findByDateBetweenAndIsHoliday(startDate, endDate, true)
            .associateBy { "${it.date} ${it.name} ${it.isHoliday}" }.toMutableMap()

        for (item in targetHolidayDataList) {
            val localDate = item.locdate.toLocalDate()

            when (val holiday = savedHolidayMap["$localDate ${item.dateName} ${item.isHoliday == "Y"}"]) {
                null -> {
                    holidaySaveList.add(
                        Holiday(
                            name = item.dateName,
                            date = localDate,
                            isHoliday = item.isHoliday == "Y"
                        )
                    )
                }

                else -> {
                    if (holiday.updateHoliday(
                            item.dateName,
                            localDate,
                            item.isHoliday == "Y"
                        )
                    ) holidayUpdateList.add(holiday)
                    savedHolidayMap.remove("$localDate ${item.dateName} ${item.isHoliday == "Y"}")
                }
            }

        }

        holidayDeleteSet.addAll(savedHolidayMap.values)
    }


    fun upsertHolidayByWeekend(
        standardYear: Int,
        holidaySaveList: MutableCollection<Holiday>,
        holidayDeleteSet: MutableCollection<Holiday>
    ) {
        val startDate = LocalDate.of(standardYear, 1, 1)
        val endDate = LocalDate.of(standardYear, 12, 31)

        val savedHolidayMap = holidayRepository.findByDateBetweenAndIsHoliday(startDate, endDate, false)
            .associateBy { it.date }.toMutableMap()

        var currentDate = startDate
        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            if (currentDate.dayOfWeek.value in listOf(6, 7)) {
                when (savedHolidayMap[currentDate]) {
                    null ->
                        holidaySaveList.add(
                            Holiday(
                                name = "주말",
                                date = currentDate,
                                isHoliday = false
                            )
                        )

                    else -> savedHolidayMap.remove(currentDate)
                }
            }


            currentDate = currentDate.plusDays(1)
        }

        holidayDeleteSet.addAll(savedHolidayMap.values)
    }

}