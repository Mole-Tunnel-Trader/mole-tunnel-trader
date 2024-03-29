package com.zeki.kisvolkotlin.domain.kis.stock_price

import com.zeki.kisvolkotlin.domain._common.aop.GetToken
import com.zeki.kisvolkotlin.domain._common.webclient.ApiStatics
import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service


@Service
class TradeWebClientService(
    private val webClientConnector: WebClientConnector,
    private val apiStatics: ApiStatics,
    private val env: Environment
) {

    @GetToken
    fun orderStock() {

    }

    @GetToken
    fun sellStock() {

    }
}