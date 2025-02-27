package com.zeki.kisserver.domain.kis.token

import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.common.util.CustomUtils
import com.zeki.mole_tunnel_db.dto.KisTokenResDto
import com.zeki.mole_tunnel_db.entity.Token
import com.zeki.mole_tunnel_db.repository.TokenRepository
import com.zeki.ok_http_client.ApiStatics
import com.zeki.ok_http_client.OkHttpClientConnector
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class TokenService(
    private val tokenRepository: TokenRepository,

    private val apiStatics: ApiStatics,
    private val okHttpClientConnector: OkHttpClientConnector,

    private val env: Environment,
) {

    // TODO : cache 적용
    @Transactional
    fun getOrCreateToken(): Token {
        val tradeMode = CustomUtils.nowTradeMode(env)

        val token = tokenRepository.findFirstByTradeModeOrderByExpiredDateDesc(tradeMode)

        return token?.takeIf { !it.isExpired() }
            ?: createToken()
    }

    fun createToken(): Token {
        val kisTokenResDto = this.getTokenFromKis()

        val token = Token(
            tokenType = kisTokenResDto.tokenType,
            tokenValue = kisTokenResDto.accessToken,
            tradeMode = CustomUtils.nowTradeMode(env),
            expiredDate = LocalDateTime.parse(
                kisTokenResDto.accessTokenTokenExpired,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).minusHours(1)
        )

        return tokenRepository.save(token)
    }

    fun getTokenFromKis(): KisTokenResDto {
        val reqBody: Map<String, String> = mapOf(
            "grant_type" to "client_credentials",
            "appkey" to apiStatics.kis.appKey,
            "appsecret" to apiStatics.kis.appSecret
        )

        val responseDatas = okHttpClientConnector.connect(
            clientType = OkHttpClientConnector.ClientType.KIS,
            method = HttpMethod.POST,
            path = "/oauth2/tokenP",
            requestBody = reqBody,
            responseClassType = KisTokenResDto::class.java,
            retryDelay = 510
        )

        return responseDatas.body ?: throw ApiException(
            ResponseCode.INTERNAL_SERVER_OK_CLIENT_ERROR,
            "토큰 발급 실패"
        )
    }

}