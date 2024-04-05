package com.zeki.kisserver.domain.kis.token.extend


import com.zeki.kisserver.domain.kis.token.TokenService
import com.zeki.kisserver.utils.TestUtils
import com.zeki.token.KisTokenResDto
import com.zeki.token.TokenRepository
import com.zeki.webclient.ApiStatics
import com.zeki.webclient.WebClientConnector
import org.springframework.core.env.Environment

class ExtendTokenService(
    tokenRepository: TokenRepository,
    apiStatics: ApiStatics,
    webClientConnector: WebClientConnector,
    env: Environment,
) : TokenService(tokenRepository, apiStatics, webClientConnector, env) {


    override fun getTokenFromKis(): KisTokenResDto {
        val filePath = "src/test/resources/token/json_response/20240325T123927TokenRes.json"

        return TestUtils.loadJsonData<KisTokenResDto>(filePath)
    }
}