package com.zeki.kisvolkotlin.domain.kis.token.extend

import com.zeki.kisserver.db.repository.TokenRepository
import com.zeki.kisserver.domain._common.webclient.WebClientConnector
import com.zeki.kisserver.domain.kis.token.TokenService
import com.zeki.kisserver.domain.kis.token.dto.KisTokenResDto
import com.zeki.kisvolkotlin.domain._common.webclient.ApiStatics
import com.zeki.kisvolkotlin.domain.kis.token.TokenService
import com.zeki.kisvolkotlin.domain.kis.token.dto.KisTokenResDto
import com.zeki.kisvolkotlin.utils.TestUtils
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