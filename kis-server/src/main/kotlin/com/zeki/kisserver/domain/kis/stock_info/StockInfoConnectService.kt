package com.zeki.kisserver.domain.kis.stock_info

import com.zeki.common.em.TradeMode
import com.zeki.kisserver.domain.kis.account.AccountService
import com.zeki.mole_tunnel_db.dto.KisStockInfoResDto
import com.zeki.ok_http_client.OkHttpClientConnector
import mu.KotlinLogging
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 셀프 인보케이션 문제로 분리
@Service
class StockInfoConnectService(
    private val okHttpClientConnector: OkHttpClientConnector,
    private val accountService: AccountService
) {
    val log = KotlinLogging.logger {}

    fun getKisStockInfoDtoList(
        stockCodeList: List<String> = emptyList(),
        endDate: LocalDate = LocalDate.now(),
        startDate: LocalDate = endDate.minusDays(1)
    ): List<KisStockInfoResDto> {


        val batchAccount = accountService.getBatchAccount()
        accountService.retrieveAccount(batchAccount)

        val stockInfoList = mutableListOf<KisStockInfoResDto>()
        for (stockCode in stockCodeList) {
            val stockInfoResDto =
                this.getStockInfoFromKis(
                    stockCode = stockCode,
                    endDate = endDate,
                    startDate = startDate,
                    appKey = batchAccount.appKey,
                    appSecret = batchAccount.appSecret,
                    tokenType = batchAccount.tokenType,
                    tokenValue = batchAccount.accessToken,
                    accountType = batchAccount.accountType
                ) ?: continue

            stockInfoList.add(stockInfoResDto)
        }

        return stockInfoList
    }

    fun getStockInfoFromKis(
        stockCode: String,
        endDate: LocalDate = LocalDate.now(),
        startDate: LocalDate = endDate.minusDays(1),
        appKey: String,
        appSecret: String,
        tokenType: String,
        tokenValue: String,
        accountType: TradeMode
    ): KisStockInfoResDto? {

        val reqHeaders: MutableMap<String, String> = HashMap<String, String>()
            .apply {
                this["authorization"] = "$tokenType $tokenValue"
                this["tr_id"] = "FHKST03010100"
            }

        val reqParams: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>()
            .apply {
                this.add("FID_COND_MRKT_DIV_CODE", "J")
                this.add("FID_INPUT_ISCD", stockCode)
                this.add(
                    "FID_INPUT_DATE_1",
                    startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                )
                this.add(
                    "FID_INPUT_DATE_2",
                    endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                )
                this.add("FID_PERIOD_DIV_CODE", "D")
                this.add("FID_ORG_ADJ_PRC", "0")
            }

        val responseDatas = okHttpClientConnector.connectKis<Map<String, String>, KisStockInfoResDto>(
            method = HttpMethod.GET,
            requestHeaders = reqHeaders,
            requestParams = reqParams,
            path = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice",
            responseClassType = KisStockInfoResDto::class.java,
            appkey = appKey,
            appsecret = appSecret,
            accountType = accountType,
        )

        if (responseDatas.body == null) {
            log.warn { "KIS 주식 정보 조회 실패, 종목코드 : $stockCode" }
            return null
        }

        val result = responseDatas.body!!

        if (result.rtCd != "0") {
            log.error { "종목코드 : ${stockCode}, 통신에러 : ${result.msg1}" }
        }

        return result
    }
}