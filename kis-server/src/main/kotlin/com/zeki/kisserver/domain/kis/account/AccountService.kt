package com.zeki.kisserver.domain.kis.account

import com.zeki.common.em.TradeMode
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.mole_tunnel_db.dto.KisTokenResDto
import com.zeki.mole_tunnel_db.entity.Account
import com.zeki.mole_tunnel_db.repository.AccountRepository
import com.zeki.ok_http_client.OkHttpClientConnector
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
        private val accountRepository: AccountRepository,
        private val okHttpClientConnector: OkHttpClientConnector,
        private val env: Environment
) {

    companion object {
        private const val TOKEN_PATH = "/oauth2/tokenP"
        private const val GRANT_TYPE = "client_credentials"
    }

    fun retrieveAccount(accountName: String, accountType: TradeMode): Account {
        val account = accountRepository.findByAccountTypeAndAccountName(accountType, accountName).orElse(null)
                ?: throw ApiException(
                        ResponseCode.BAD_REQUEST,
                        "Account not found for accountName=$accountName and accountType=$accountType"
                )

        return if (account.accessToken == null || account.isExpired()) {
            refreshAccountToken(account)
        } else {
            account
        }
    }

    @Transactional
    fun refreshAccountToken(account: Account): Account {
        val kisTokenResDto = retrieveTokenFromKis(account.appKey, account.appSecret)
        account.updateToken(kisTokenResDto)
        return account
    }

    private fun retrieveTokenFromKis(appKey: String, appSecret: String): KisTokenResDto {
        val reqBody = buildRequestBody(appKey, appSecret)

        val response = okHttpClientConnector.connect(
                clientType = OkHttpClientConnector.ClientType.KIS,
                method = HttpMethod.POST,
                path = TOKEN_PATH,
                requestBody = reqBody,
                responseClassType = KisTokenResDto::class.java,
                retryDelay = 510
        )

        return response.body ?: throw ApiException(
                ResponseCode.INTERNAL_SERVER_WEBCLIENT_ERROR,
                "Token retrieval failed for appKey=$appKey"
        )
    }

    private fun buildRequestBody(appKey: String, appSecret: String): Map<String, String> {
        return mapOf(
                "grant_type" to GRANT_TYPE,
                "appkey" to appKey,
                "appsecret" to appSecret
        )
    }
}
