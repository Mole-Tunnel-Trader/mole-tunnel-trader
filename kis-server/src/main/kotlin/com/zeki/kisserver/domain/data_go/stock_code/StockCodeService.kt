package com.zeki.kisserver.domain.data_go.stock_code

import com.zeki.common.em.Status
import com.zeki.common.em.StockMarket
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.common.util.CustomUtils.toStringDate
import com.zeki.holiday.dto.report.UpsertReportDto
import com.zeki.holiday.service.HolidayDateService
import com.zeki.mole_tunnel_db.dto.DataGoStockCodeResDto
import com.zeki.mole_tunnel_db.dto.DataGoStockCodeResDto.StockCodeItem
import com.zeki.mole_tunnel_db.entity.StockCode
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import com.zeki.mole_tunnel_db.repository.join.StockCodeJoinRepository
import com.zeki.ok_http_client.OkHttpClientConnector
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import java.time.LocalDate
import java.time.LocalTime


@Service
class StockCodeService(
    private val stockCodeRepository: StockCodeRepository,
    private val getSTockCodeService: GetStockCodeService,
    private val stockCodeJoinRepository: StockCodeJoinRepository,

    private val holidayDateService: HolidayDateService,

    private val okHttpClientConnector: OkHttpClientConnector
) {

    @Transactional
    fun upsertStockCode(
        standardDate: LocalDate = LocalDate.now(),
        standardTime: LocalTime = LocalTime.now(),
        standardDeltaDate: Int = 10
    ): UpsertReportDto {
        val stockCodeSaveList = mutableListOf<StockCode>()
        val stockCodeUpdateList = mutableListOf<StockCode>()
        val stockCodeDeleteSet = mutableSetOf<StockCode>()

        val dataGoStockCodeItemList: List<StockCodeItem> =
            this.getStockCodesFromDataGo(
                standardDate = standardDate,
                standardTime = standardTime,
                standardDeltaDate = standardDeltaDate
            )

        val stockCodeMap = getSTockCodeService.getStockCodeList()
            .associateBy { it.code }
            .toMutableMap()

        for (item in dataGoStockCodeItemList) {
            val stockCode = item.srtnCd.substring(1)
            val stockName = item.itmsNm
            val stockMarket = item.mrktCtg

            when (val stockCodeEntity = stockCodeMap[stockCode]) {
                null -> {
                    stockCodeSaveList.add(
                        StockCode(
                            code = stockCode,
                            name = stockName,
                            market = StockMarket.valueOf(stockMarket),
                            isAlive = Status.Y
                        ),
                    )
                }

                else -> {
                    val isUpdate = stockCodeEntity.updateStockCode(
                        name = stockName,
                        market = StockMarket.valueOf(stockMarket),
                        isAlive = Status.Y
                    )
                    if (isUpdate) {
                        stockCodeUpdateList.add(stockCodeEntity)
                    }
                    stockCodeMap.remove(stockCode)
                }
            }
        }
        stockCodeDeleteSet.addAll(stockCodeMap.values)

        for (stockCode in stockCodeDeleteSet) {
            stockCode.updateIsAlive(Status.D);
        }
        stockCodeUpdateList.addAll(stockCodeDeleteSet)


        stockCodeJoinRepository.bulkInsert(stockCodeSaveList)
        stockCodeJoinRepository.bulkUpdate(stockCodeUpdateList)


        return UpsertReportDto(
            stockCodeSaveList.size,
            stockCodeUpdateList.size,
            stockCodeDeleteSet.size
        )
    }

    private fun getStockCodesFromDataGo(
        standardDate: LocalDate = LocalDate.now(),
        standardTime: LocalTime = LocalTime.now(),
        standardDeltaDate: Int = 10
    ): List<StockCodeItem> {
        var pageNo = 1
        var totalCount = Int.MAX_VALUE

        val batchSize = 1000
        val deltaOfToday: String =
            holidayDateService.getAvailableDate(
                standardDate = standardDate,
                standardTime = standardTime,
                standardDeltaDate = standardDeltaDate
            ).toStringDate()

        val queryParams = LinkedMultiValueMap<String, String>()
            .apply {
                add("resultType", "json")
                add("numOfRows", batchSize.toString())
                add("basDt", deltaOfToday)
                add("pageNo", pageNo.toString())
            }

        val dataGoStockCodeItemList = mutableListOf<StockCodeItem>()
        while ((pageNo - 1) * batchSize < totalCount) {
            queryParams["pageNo"] = pageNo.toString()

            val responseDatas = okHttpClientConnector.connect<Unit, DataGoStockCodeResDto>(
                OkHttpClientConnector.ClientType.DATA_GO,
                HttpMethod.GET,
                "1160100/service/GetKrxListedInfoService/getItemInfo",
                requestParams = queryParams,
                responseClassType = DataGoStockCodeResDto::class.java
            )

            val dataGoStockCodeResDto =
                responseDatas.body ?: throw ApiException(
                    ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR,
                    "통신에러 queryParams: $queryParams"
                )

            totalCount = dataGoStockCodeResDto.response.body.totalCount
            pageNo += 1

            dataGoStockCodeItemList.addAll(dataGoStockCodeResDto.response.body.items.item)
        }

        return dataGoStockCodeItemList
    }

}
