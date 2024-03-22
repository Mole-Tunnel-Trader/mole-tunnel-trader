package com.zeki.kisvolkotlin.domain.data_go.stock_code

import com.zeki.kisvolkotlin.db.entity.StockCode
import com.zeki.kisvolkotlin.db.entity.em.StockMarket
import com.zeki.kisvolkotlin.db.repository.StockCodeJoinRepository
import com.zeki.kisvolkotlin.db.repository.StockCodeRepository
import com.zeki.kisvolkotlin.domain._common.util.CustomUtils.toStringDate
import com.zeki.kisvolkotlin.domain.data_go.holiday.HolidayDateService
import com.zeki.kisvolkotlin.domain.data_go.stock_code.dto.DataGoStockCodeResDto
import com.zeki.kisvolkotlin.domain.data_go.stock_code.dto.StockCodeItem
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.retry.Retry
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

@Service
class StockCodeService(
    private val stockCodeRepository: StockCodeRepository,
    private val stockCodeJoinRepository: StockCodeJoinRepository,

    private val holidayDateService: HolidayDateService,

    @Qualifier("WebClientDataGo") private val webClientDataGo: WebClient
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


    fun getStockCodesFromDataGo(
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
            )
                .toStringDate()

        val reqParam = LinkedMultiValueMap<String, String>()
        reqParam["resultType"] = "json"
        reqParam["numOfRows"] = batchSize.toString()
        reqParam["basDt"] = deltaOfToday
        reqParam["pageNo"] = pageNo.toString()

        val dataGoStockCodeItemList = mutableListOf<StockCodeItem>()
        while ((pageNo - 1) * batchSize < totalCount) {
            reqParam["pageNo"] = pageNo.toString()

            val responseDatas = webClientDataGo.get()
                .uri {
                    it.path("1160100/service/GetKrxListedInfoService/getItemInfo")
                        .queryParams(reqParam)
                        .build()
                }
                .exchangeToMono { it.toEntity(DataGoStockCodeResDto::class.java) }
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(510)))
                .block()

            val dataGoStockCodeResDto = responseDatas?.body ?: DataGoStockCodeResDto()

            totalCount = dataGoStockCodeResDto.response.body.totalCount
            pageNo += 1

            dataGoStockCodeItemList.addAll(dataGoStockCodeResDto.response.body.items.item)
        }

        return dataGoStockCodeItemList
    }
}
