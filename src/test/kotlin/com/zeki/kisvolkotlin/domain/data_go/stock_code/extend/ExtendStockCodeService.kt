package com.zeki.kisvolkotlin.domain.data_go.stock_code.extend

import com.zeki.kisvolkotlin.db.repository.StockCodeJoinRepository
import com.zeki.kisvolkotlin.db.repository.StockCodeRepository
import com.zeki.kisvolkotlin.domain.data_go.holiday.HolidayDateService
import com.zeki.kisvolkotlin.domain.data_go.stock_code.StockCodeService
import com.zeki.kisvolkotlin.domain.data_go.stock_code.dto.DataGoStockCodeResDto
import com.zeki.kisvolkotlin.domain.data_go.stock_code.dto.StockCodeItem
import com.zeki.kisvolkotlin.utils.TestUtils
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.time.LocalTime


class ExtendStockCodeService(
    stockCodeRepository: StockCodeRepository,
    stockCodeJoinRepository: StockCodeJoinRepository,
    holidayDateService: HolidayDateService,
    webClientDataGo: WebClient
) : StockCodeService(
    stockCodeRepository,
    stockCodeJoinRepository,
    holidayDateService,
    webClientDataGo
) {

    override fun getStockCodesFromDataGo(
        standardDate: LocalDate,
        standardTime: LocalTime,
        standardDeltaDate: Int
    ): List<StockCodeItem> {
        val result = mutableListOf<StockCodeItem>()

        val filePath01 = "src/test/resources/stock_code/json_response/20240304StockCodeRes-01.json"
        val filePath02 = "src/test/resources/stock_code/json_response/20240304StockCodeRes-02.json"
        val filePath03 = "src/test/resources/stock_code/json_response/20240304StockCodeRes-03.json"

        listOf(
            TestUtils.loadJsonData<DataGoStockCodeResDto>(filePath01),
            TestUtils.loadJsonData<DataGoStockCodeResDto>(filePath02),
            TestUtils.loadJsonData<DataGoStockCodeResDto>(filePath03)
        ).forEach {
            result.addAll(it.response.body.items.item)
        }

        return result
    }
}
