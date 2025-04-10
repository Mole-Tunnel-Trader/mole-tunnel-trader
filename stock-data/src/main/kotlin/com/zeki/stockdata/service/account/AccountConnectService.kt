package com.zeki.stockdata.service.account

import com.zeki.common.em.TradeMode
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.mole_tunnel_db.dto.KisTokenResDto
import com.zeki.ok_http_client.OkHttpClientConnector
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service

@Service
class AccountConnectService(
    private val okHttpClientConnector: OkHttpClientConnector,
) {


    fun retrieveTokenFromKis(
        appKey: String,
        appSecret: String,
        accountType: TradeMode,
        grantType: String
    ): KisTokenResDto {
        val reqBody = mapOf(
            "appkey" to appKey,
            "appsecret" to appSecret,
            "grant_type" to grantType,
        )

        val response = okHttpClientConnector.connectKis(
            method = HttpMethod.POST,
            path = "/oauth2/tokenP",
            requestBody = reqBody,
            responseClassType = KisTokenResDto::class.java,
            appkey = appKey,
            appsecret = appSecret,
            accountType = accountType,
        )

        return response.body ?: throw ApiException(
            ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR,
            "Token retrieval failed for appKey=$appKey"
        )
    }
}