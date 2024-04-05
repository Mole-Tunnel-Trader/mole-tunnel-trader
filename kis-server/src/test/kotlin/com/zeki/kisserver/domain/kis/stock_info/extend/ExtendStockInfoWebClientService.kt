package com.zeki.kisserver.domain.kis.stock_info.extend

import com.zeki.common.em.TradeMode
import com.zeki.kisserver.domain._common.aop.TokenHolder
import com.zeki.kisserver.domain.kis.stock_info.StockInfoWebClientService

import com.zeki.kisserver.utils.TestUtils
import com.zeki.stockdata.stock_info.KisStockInfoResDto
import com.zeki.token.Token
import com.zeki.webclient.ApiStatics
import com.zeki.webclient.WebClientConnector
import java.time.LocalDate
import java.time.LocalDateTime

class ExtendStockInfoWebClientService(
    apiStatics: ApiStatics,
    webClientConnector: WebClientConnector
) : StockInfoWebClientService(
    apiStatics,
    webClientConnector
) {

    override fun getKisStockInfoDtoList(
        stockCodeList: List<String>,
        endDate: LocalDate,
        startDate: LocalDate
    ): List<KisStockInfoResDto> {

        TokenHolder.setToken(
            Token(
                tokenType = "test",
                tokenValue = "test",
                tradeMode = TradeMode.TRAIN,
                expiredDate = LocalDateTime.now().plusDays(1)
            )
        )

        return super.getKisStockInfoDtoList(stockCodeList, endDate, startDate)
    }

    override fun getStockInfoFromKis(
        stockCode: String,
        endDate: LocalDate,
        startDate: LocalDate,
        tokenType: String,
        tokenValue: String
    ): KisStockInfoResDto {
        return when (stockCode) {
            "000020" -> TestUtils.loadJsonData<KisStockInfoResDto>("src/test/resources/stock_info/json_response/StockInfoRes-000020.json")
            "000040" -> TestUtils.loadJsonData<KisStockInfoResDto>("src/test/resources/stock_info/json_response/StockInfoRes-000040.json")
            else -> throw IllegalArgumentException("해당 데이터에 맞는 파일이 없습니다. stockCode: $stockCode")
        }
    }
}