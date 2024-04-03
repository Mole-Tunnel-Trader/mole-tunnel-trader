package com.zeki.kisserver.domain.kis.token

import com.zeki.common.util.CustomUtils
import com.zeki.exception.ResponseCode
import com.zeki.kisserver.db.repository.TokenRepository
import com.zeki.kisserver.domain._common.util.CustomUtils
import com.zeki.kisserver.domain._common.webclient.ApiStatics
import com.zeki.kisserver.domain._common.webclient.WebClientConnector
import com.zeki.kisserver.domain.kis.token.dto.KisTokenResDto
import com.zeki.kisserver.exception.ResponseCode
import com.zeki.kisvolkotlin.domain._common.util.CustomUtils
import com.zeki.kisvolkotlin.exception.ResponseCode
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
    private val webClientConnector: WebClientConnector,

    private val env: Environment,
) {

    // TODO : cache 적용
    @Transactional
    fun getOrCreateToken(): com.zeki.kisserver.db.entity.Token {
        val tradeMode = CustomUtils.nowTradeMode(env)

        val token = tokenRepository.findFirstByTradeModeOrderByExpiredDateDesc(tradeMode)

        return token?.takeIf { !it.isExpired() }
            ?: createToken()
    }

    fun createToken(): com.zeki.kisserver.db.entity.Token {
        val kisTokenResDto = this.getTokenFromKis()

        val token = com.zeki.kisserver.db.entity.Token(
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

        val responseDatas = webClientConnector.connect(
            webClientType = WebClientConnector.WebClientType.KIS,
            method = HttpMethod.POST,
            path = "/oauth2/tokenP",
            requestBody = reqBody,
            responseClassType = KisTokenResDto::class.java
        )

        return responseDatas?.body ?: throw com.zeki.kisserver.exception.ApiException(
            ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR,
            "토큰 발급 실패"
        )
    }

}