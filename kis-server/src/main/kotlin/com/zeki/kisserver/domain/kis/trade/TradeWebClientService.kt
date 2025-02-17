package com.zeki.kisserver.domain.kis.trade

import com.zeki.common.em.OrderType
import com.zeki.common.em.TradeMode
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.common.util.CustomUtils
import com.zeki.kisserver.domain._common.aop.GetToken
import com.zeki.kisserver.domain._common.aop.TokenHolder
import com.zeki.mole_tunnel_db.dto.KisOrderStockResDto
import com.zeki.webclient.ApiStatics
import com.zeki.webclient.WebClientConnector
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class TradeWebClientService(
    private val webClientConnector: WebClientConnector,
    private val apiStatics: ApiStatics,
    private val env: Environment
) {

    @GetToken
    fun orderStock(
        orderType: OrderType,
        stockCode: String,
        orderPrice: BigDecimal,
        orderAmount: Double
    ): KisOrderStockResDto {
        val token = TokenHolder.getToken()

        val reqBody: MutableMap<String, String> = HashMap<String, String>().apply {
            this["CANO"] = apiStatics.kis.accountNumber
            this["ACNT_PRDT_CD"] = "01"
            this["PDNO"] = stockCode
            this["ORD_DVSN"] = if (orderPrice == BigDecimal.ZERO) "01" else "00"
            this["ORD_QTY"] = orderAmount.toString()
            this["ORD_UNPR"] = orderPrice.toString()
        }

        val reqHeader: MutableMap<String, String> = HashMap<String, String>().apply {
            this["authorization"] = "${token.tokenType} ${token.tokenValue}"
            this["appkey"] = apiStatics.kis.appKey
            this["appsecret"] = apiStatics.kis.appSecret
            when (orderType) {
                OrderType.BUY -> this["tr_id"] =
                    if (CustomUtils.nowTradeMode(env) == TradeMode.REAL) "TTTC0802U" else "VTTC0802U"

                OrderType.SELL -> this["tr_id"] =
                    if (CustomUtils.nowTradeMode(env) == TradeMode.REAL) "TTTC0801U" else "VTTC0801U"
            }
        }

        val responsesDatas = webClientConnector.connect<Map<String, String>, KisOrderStockResDto>(
            WebClientConnector.WebClientType.KIS,
            HttpMethod.POST,
            "/uapi/domestic-stock/v1/trading/order-cash",
            requestHeaders = reqHeader,
            requestBody = reqBody,
            responseClassType = KisOrderStockResDto::class.java,
            retryCount = 0,
            retryDelay = 0,
        )

        return responsesDatas?.body ?: throw ApiException(
            ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR,
            "주식 주문 실패. 주식코드: $stockCode, 주문타입: ${orderType.name}, 주문가격: $orderPrice, 주문수량: $orderAmount"
        )
    }

}
