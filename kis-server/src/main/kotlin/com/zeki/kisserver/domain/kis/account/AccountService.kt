package com.zeki.kisserver.domain.kis.account

import com.zeki.common.em.TradeMode
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.mole_tunnel_db.entity.Account
import com.zeki.mole_tunnel_db.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountConnectService: AccountConnectService,

    private val accountRepository: AccountRepository
) {
    @Transactional
    fun retrieveAccount(account: Account): Unit {
        if (account.isExpired()) {
            refreshAccountToken(account)
            accountRepository.save(account)
        }
    }

    private fun refreshAccountToken(account: Account): Account {
        val kisTokenResDto =
            accountConnectService.retrieveTokenFromKis(account.appKey, account.appSecret, account.accountType)
        account.updateToken(kisTokenResDto)
        return account
    }


    @Transactional(readOnly = true)
    public fun getBatchAccount(): Account {
        return accountRepository.findByAccountType(TradeMode.BATCH).firstOrNull()
            ?: throw ApiException(ResponseCode.RESOURCE_NOT_FOUND, "Batch 계좌를 찾을 수 없습니다.")
    }
}
