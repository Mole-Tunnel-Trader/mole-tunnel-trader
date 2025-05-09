package com.zeki.stockdata.service.stock_info

import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.common.util.CustomUtils.toLocalDate
import com.zeki.common.util.CustomUtils.toStringDate
import com.zeki.mole_tunnel_db.dto.NaverStockPriceResDto
import com.zeki.ok_http_client.OkHttpClientConnector
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.time.LocalDate
import java.util.regex.Pattern

@Service
class CrawlNaverFinanceService(
    private val okHttpClientConnector: OkHttpClientConnector
) {
    private val log = mu.KotlinLogging.logger {}

    fun crawlStockPrice(stockCode: String, stdDay: LocalDate, count: Int): NaverStockPriceResDto {
        val requestType = "2"
        val startDate = stdDay.toStringDate("yyyyMMdd")
        val timeframe = "day"

        val reqParam: MultiValueMap<String, String> = LinkedMultiValueMap()

        reqParam.add("symbol", stockCode)
        reqParam.add("requestType", requestType)
        reqParam.add("count", count.toString())
        reqParam.add("startTime", startDate)
        reqParam.add("timeframe", timeframe)

        sleep(110L)
        val responseDatas = okHttpClientConnector.connect<Unit, String>(
            OkHttpClientConnector.ClientType.NAVER_FINANCE,
            HttpMethod.GET,
            "https://api.finance.naver.com/siseJson.naver",
            requestParams = reqParam,
            responseClassType = String::class.java
        )

        val result =
            responseDatas.body ?: throw ApiException(
                ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR,
                "네이버 금융 API 호출 실패"
            )
        return this.toNaverDto(result, stockCode)
    }


    fun toNaverDto(response: String, stockCode: String): NaverStockPriceResDto {
        val itemList = mutableListOf<NaverStockPriceResDto.Item>()
        val result = NaverStockPriceResDto(
            stockCode = stockCode,
            items = itemList
        )

        val p = Pattern.compile("\\[([^\\[\\]]*)]")
        val m = p.matcher(response)

        // 첫 번째 라인 스킵
        if (m.find()) {
            m.group(1)
        }

        while (m.find()) {
            val items =
                m.group(1).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val row: MutableList<Any> = ArrayList()

            for (i in 0 until items.size - 1) {
                val item = items[i].replace("[^0-9.+-]".toRegex(), "")

                when (i) {
                    0 -> {
                        row.add(item.toLocalDate("yyyyMMdd"))
                    }

                    items.size - 2 -> {
                        row.add(item.toLong())
                    }

                    else -> {
                        row.add(BigDecimal(item))
                    }
                }
            }

            val date = row[0] as LocalDate
            val closePrice = row[4] as BigDecimal
            val openPrice = row[1] as BigDecimal
            val highPrice = row[2] as BigDecimal
            val lowPrice = row[3] as BigDecimal
            val volume = row[5] as Long

            itemList.add(
                NaverStockPriceResDto.Item(
                    date = date,
                    close = closePrice,
                    open = openPrice,
                    high = highPrice,
                    low = lowPrice,
                    volume = volume
                )
            )
        }

        return result
    }

}