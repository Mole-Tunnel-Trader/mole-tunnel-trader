package com.zeki.kisserver.domain.kis.trade

import com.zeki.common.em.OrderType
import com.zeki.common.em.TradeMode
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.kisserver.domain.kis.account.AccountService
import com.zeki.mole_tunnel_db.dto.KisOrderStockResDto
import com.zeki.mole_tunnel_db.entity.Account
import com.zeki.ok_http_client.OkHttpClientConnector
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class TradeConnectService(
    private val okHttpClientConnector: OkHttpClientConnector,
    private val accountService: AccountService,
) {

    fun orderStock(
        orderType: OrderType,
        stockCode: String,
        orderPrice: BigDecimal,
        orderAmount: Double,
        account: Account
    ): KisOrderStockResDto {

        accountService.retrieveAccount(account)

        val reqBody: MutableMap<String, String> = HashMap<String, String>().apply {
            this["CANO"] = account.accountNumber
            this["ACNT_PRDT_CD"] = "01"
            this["PDNO"] = stockCode
            this["ORD_DVSN"] = if (orderPrice == BigDecimal.ZERO) "01" else "00"
            this["ORD_QTY"] = orderAmount.toString()
            this["ORD_UNPR"] = orderPrice.toString()
        }

        val reqHeader: MutableMap<String, String> = HashMap<String, String>().apply {
            this["authorization"] = "${account.tokenType} ${account.accessToken}"
            when (orderType) {
                OrderType.BUY -> this["tr_id"] =
                    if (account.accountType == TradeMode.REAL) "TTTC0802U" else "VTTC0802U"

                OrderType.SELL -> this["tr_id"] =
                    if (account.accountType == TradeMode.REAL) "TTTC0801U" else "VTTC0801U"
            }
        }

        val responsesDatas =
            okHttpClientConnector.connectKis<Map<String, String>, KisOrderStockResDto>(
                HttpMethod.POST,
                "/uapi/domestic-stock/v1/trading/order-cash",
                requestHeaders = reqHeader,
                requestBody = reqBody,
                responseClassType = KisOrderStockResDto::class.java,
                appkey = account.appKey,
                appsecret = account.appSecret,
                accountType = account.accountType,
            )

        return responsesDatas.body ?: throw ApiException(
            ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR,
            "주식 주문 실패. 주식코드: $stockCode, 주문타입: ${orderType.name}, 주문가격: $orderPrice, 주문수량: $orderAmount"
        )
    }

}
