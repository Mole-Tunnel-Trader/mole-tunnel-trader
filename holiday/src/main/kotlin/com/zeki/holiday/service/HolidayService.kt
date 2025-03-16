package com.zeki.holiday.service

// import com.zeki.holiday.Holiday
// import com.zeki.holiday.HolidayJoinRepository
// import com.zeki.holiday.HolidayRepository

import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.common.util.CustomUtils.toLocalDate
import com.zeki.holiday.dto.report.UpsertReportDto
import com.zeki.mole_tunnel_db.dto.DataGoHolidayResDto
import com.zeki.mole_tunnel_db.entity.Holiday
import com.zeki.mole_tunnel_db.repository.HolidayRepository
import com.zeki.mole_tunnel_db.repository.join.HolidayJoinRepository
import com.zeki.ok_http_client.OkHttpClientConnector
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

    private val okHttpClientConnector: OkHttpClientConnector,
) {

    /**
     * 2년치 휴일데이터 생성
     */
    @Transactional
    fun upsertHoliday(standardYear: Int? = null): UpsertReportDto {
        val standardYear = standardYear ?: holidayDateService.getAvailableDate().year
        val holidaySaveList = mutableListOf<Holiday>()
        val holidayUpdateList = mutableListOf<Holiday>()
        val holidayDeleteSet = mutableSetOf<Holiday>()

        this.upsertHolidayByDataGo(
            standardYear,
            holidaySaveList,
            holidayUpdateList,
            holidayDeleteSet
        )
        this.upsertHolidayByDataGo(
            standardYear + 1,
            holidaySaveList,
            holidayUpdateList,
            holidayDeleteSet
        )

        this.upsertHolidayByWeekend(standardYear, holidaySaveList, holidayDeleteSet)
        this.upsertHolidayByWeekend(standardYear + 1, holidaySaveList, holidayDeleteSet)


        holidayJoinRepository.bulkInsert(holidaySaveList)
        holidayJoinRepository.bulkUpdate(holidayUpdateList)
        holidayRepository.deleteAllInBatch(holidayDeleteSet)

        return UpsertReportDto(
            newCount = holidaySaveList.size,
            updateCount = holidayUpdateList.size,
            deleteCount = holidayDeleteSet.size
        )
    }

    fun getHolidaysFromDataGo(standardYear: Int? = null)
            : DataGoHolidayResDto {
        val standardYear = standardYear ?: holidayDateService.getAvailableDate().year

        val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>()
            .apply {
                add("solYear", standardYear.toString())
                add("_type", "json")
                add("numOfRows", "100")
            }

        val responseDatas =
            okHttpClientConnector.connect<Unit, DataGoHolidayResDto>(
                OkHttpClientConnector.ClientType.DATA_GO,
                HttpMethod.GET,
                "B090041/openapi/service/SpcdeInfoService/getRestDeInfo",
                requestParams = queryParams,
                responseClassType = DataGoHolidayResDto::class.java
            )

        val dataGoHolidayResDto =
            responseDatas.body ?: DataGoHolidayResDto()

        if (dataGoHolidayResDto.response.header.resultCode != "00") {
            throw ApiException(
                ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR,
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

        val savedHolidayMap =
            holidayRepository.findByDateBetweenAndIsHoliday(startDate, endDate, true)
                .associateBy { "${it.date} ${it.name} ${it.isHoliday}" }.toMutableMap()

        for (item in targetHolidayDataList) {
            val localDate = item.locdate.toLocalDate()

            when (val holiday =
                savedHolidayMap["$localDate ${item.dateName} ${item.isHoliday == "Y"}"]) {
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

        val savedHolidayMap =
            holidayRepository.findByDateBetweenAndIsHoliday(startDate, endDate, false)
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

    @Transactional(readOnly = true)
    fun getHolidayList(startDate: LocalDate, endDate: LocalDate): List<Holiday> {
        return holidayRepository.findByDateBetweenOrderByDateAsc(startDate, endDate)
    }
}