package com.zeki.kisserver.domain.kis.stock_info

import com.zeki.common.em.TradeMode
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.kisserver.domain.kis.account.AccountService
import com.zeki.kisserver.domain.kis.stock_info.dto.KisAssetResDto
import com.zeki.mole_tunnel_db.entity.Account
import com.zeki.ok_http_client.OkHttpClientConnector
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@Service
class AssetWebClientService(
    private val okHttpClientConnector: OkHttpClientConnector,
    private val accountService: AccountService,
    private val env: Environment
) {

    fun getAccountData(account: Account): List<KisAssetResDto.Output1> {

        // TODO : 모의투자 주식 매매 후 테스트
        val accessToken = accountService.retrieveAccount(account)

        val reqHeaders: MutableMap<String, String> = HashMap<String, String>().apply {
            this["authorization"] = "${account.tokenType} ${accessToken}"
            this["appkey"] = account.appKey
            this["appsecret"] = account.appSecret
            this["tr_id"] =
                if (account.accountType == TradeMode.REAL) "TTTC8434R" else "VTTC8434R"
        }

        val reqParams: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("CANO", account.accountNumber)
            add("ACNT_PRDT_CD", "01")
            add("AFHR_FLPR_YN", "N")
            add("OFL_YN", "")
            add("INQR_DVSN", "01")
            add("UNPR_DVSN", "01")
            add("FUND_STTL_ICLD_YN", "N")
            add("FNCG_AMT_AUTO_RDPT_YN", "N")
            add("PRCS_DVSN", "01")
            add("CTX_AREA_FK100", "")
            add("CTX_AREA_NK100", "")
        }

        val resultList: MutableList<KisAssetResDto.Output1> = ArrayList()

        var trCont = "F"

        while (trCont == "F" || trCont == "M") {
            val responseDatas = okHttpClientConnector.connect<Unit, KisAssetResDto>(
                OkHttpClientConnector.ClientType.KIS,
                HttpMethod.GET,
                "/uapi/domestic-stock/v1/trading/inquire-balance",
                reqHeaders,
                reqParams,
                responseClassType = KisAssetResDto::class.java,
                retryCount = 1,
                retryDelay = 510
            )

            val kisAssetResDto = responseDatas.body ?: throw ApiException(
                ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR,
                "WebClient 통신 에러"
            )

            val tempTrCont = responseDatas.headers?.getOrDefault("tr_cont", listOf(""))
            trCont = tempTrCont?.get(0) ?: "0"
            resultList.addAll(kisAssetResDto.output1)

            reqParams["CTX_AREA_FK100"] =
                if (trCont == "F" || trCont == "M") kisAssetResDto.ctxAreaFk100 else ""
            reqParams["CTX_AREA_NK100"] =
                if (trCont == "F" || trCont == "M") kisAssetResDto.ctxAreaNk100 else ""
        }

        return resultList
    }

}