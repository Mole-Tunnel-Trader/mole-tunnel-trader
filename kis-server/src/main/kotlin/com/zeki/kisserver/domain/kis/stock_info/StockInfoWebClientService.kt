package com.zeki.kisserver.domain.kis.stock_info

import com.zeki.common.exception.ExceptionUtils.log
import com.zeki.kisserver.domain._common.aop.GetToken
import com.zeki.kisserver.domain._common.aop.TokenHolder
import com.zeki.stockdata.stock_info.KisStockInfoResDto
import com.zeki.webclient.ApiStatics
import com.zeki.webclient.WebClientConnector
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 셀프 인보케이션 문제로 분리
@Service
class StockInfoWebClientService(
    private val apiStatics: ApiStatics,
    private val webClientConnector: WebClientConnector
) {

    @GetToken
    fun getKisStockInfoDtoList(
        stockCodeList: List<String> = emptyList(),
        endDate: LocalDate = LocalDate.now(),
        startDate: LocalDate = endDate.minusDays(1)
    ): List<KisStockInfoResDto> {

        val token = TokenHolder.getToken()

        val stockInfoList = mutableListOf<KisStockInfoResDto>()
        for (stockCode in stockCodeList) {
            val stockInfoResDto =
                this.getStockInfoFromKis(stockCode, endDate, startDate, token.tokenType, token.tokenValue) ?: continue
            stockInfoList.add(stockInfoResDto)
        }

        return stockInfoList
    }

    // 2600건 정보 조회하므로 @GetToken은 상위 메서드에 작성
    fun getStockInfoFromKis(
        stockCode: String,
        endDate: LocalDate = LocalDate.now(),
        startDate: LocalDate = endDate.minusDays(1),
        tokenType: String,
        tokenValue: String
    ): KisStockInfoResDto? {

        val reqHeaders: MutableMap<String, String> = HashMap<String, String>()
            .apply {
                this["authorization"] = "$tokenType $tokenValue"
                this["appkey"] = apiStatics.kis.appKey
                this["appsecret"] = apiStatics.kis.appSecret
                this["tr_id"] = "FHKST03010100"
            }

        val reqParams: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>()
            .apply {
                this.add("FID_COND_MRKT_DIV_CODE", "J")
                this.add("FID_INPUT_ISCD", stockCode)
                this.add("FID_INPUT_DATE_1", startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                this.add("FID_INPUT_DATE_2", endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                this.add("FID_PERIOD_DIV_CODE", "D")
                this.add("FID_ORG_ADJ_PRC", "0")
            }

        val responseDatas = webClientConnector.connect<Map<String, String>, KisStockInfoResDto>(
            webClientType = WebClientConnector.WebClientType.KIS,
            method = HttpMethod.POST,
            requestHeaders = reqHeaders,
            requestParams = reqParams,
            path = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice",
            responseClassType = KisStockInfoResDto::class.java,
            retryCount = 1,
            retryDelay = 510
        )

        if (responseDatas == null || responseDatas.body == null) {
            log.warn { "KIS 주식 정보 조회 실패, 종목코드 : $stockCode" }
            return null
        }

        val result = responseDatas!!.body!!

        if (result.rtCd != "0") {
            // TODO : 에러 발생시 전체 장애로 이어지므로 WebHook 처리
            println("종목코드 : ${stockCode}, 통신에러 : ${result.msg1}")
        }

        return result
    }
}