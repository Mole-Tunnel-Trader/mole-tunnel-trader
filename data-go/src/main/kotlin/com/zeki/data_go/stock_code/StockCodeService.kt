package com.zeki.data_go.stock_code

import com.zeki.common.em.StockMarket
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.common.util.CustomUtils.toStringDate
import com.zeki.data_go.holiday.HolidayDateService
import com.zeki.mole_tunnel_db.dto.DataGoStockCodeResDto
import com.zeki.mole_tunnel_db.dto.DataGoStockCodeResDto.StockCodeItem
import com.zeki.mole_tunnel_db.entity.StockCode
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import com.zeki.mole_tunnel_db.repository.join.StockCodeJoinRepository
import com.zeki.webclient.WebClientConnector
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import java.time.LocalDate
import java.time.LocalTime


@Service
class StockCodeService(
        private val stockCodeRepository: StockCodeRepository,
        private val stockCodeJoinRepository: StockCodeJoinRepository,

        private val holidayDateService: HolidayDateService,

        private val webClientConnector: WebClientConnector
) {

    @Transactional
    fun upsertStockCode(
            standardDate: LocalDate = LocalDate.now(),
            standardTime: LocalTime = LocalTime.now(),
            standardDeltaDate: Int = 10
    ) {
        val stockCodeSaveList = mutableListOf<StockCode>()
        val stockCodeUpdateList = mutableListOf<StockCode>()
        val stockCodeDeleteSet = mutableSetOf<StockCode>()

        val dataGoStockCodeItemList =
                this.getStockCodesFromDataGo(
                        standardDate = standardDate,
                        standardTime = standardTime,
                        standardDeltaDate = standardDeltaDate
                )

        val stockCodeMap = stockCodeRepository.findAll()
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
                                    market = StockMarket.valueOf(stockMarket)
                            )
                    )
                }

                else -> {
                    val isUpdate = stockCodeEntity.updateStockCode(
                            name = stockName,
                            market = StockMarket.valueOf(stockMarket)
                    )
                    if (isUpdate) {
                        stockCodeUpdateList.add(stockCodeEntity)
                    }
                    stockCodeMap.remove(stockCode)
                }
            }
        }

        stockCodeDeleteSet.addAll(stockCodeMap.values)

        stockCodeJoinRepository.bulkInsert(stockCodeSaveList)
        stockCodeJoinRepository.bulkUpdate(stockCodeUpdateList)
        stockCodeRepository.deleteAllInBatch(stockCodeDeleteSet)
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

            val responseDatas = webClientConnector.connect<Unit, DataGoStockCodeResDto>(
                    WebClientConnector.WebClientType.DATA_GO,
                    HttpMethod.GET,
                    "1160100/service/GetKrxListedInfoService/getItemInfo",
                    requestParams = queryParams,
                    responseClassType = DataGoStockCodeResDto::class.java,
                    retryCount = 3,
                    retryDelay = 510
            )

            val dataGoStockCodeResDto =
                    responseDatas?.body ?: throw ApiException(
                            ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR,
                            "통신에러 queryParams: $queryParams"
                    )

            totalCount = dataGoStockCodeResDto.response.body.totalCount
            pageNo += 1

            dataGoStockCodeItemList.addAll(dataGoStockCodeResDto.response.body.items.item)
        }

        return dataGoStockCodeItemList
    }

    @Transactional(readOnly = true)
    fun getStockCodeList(): List<String> {
        return stockCodeRepository.findAll().map { it.code }
    }
}
