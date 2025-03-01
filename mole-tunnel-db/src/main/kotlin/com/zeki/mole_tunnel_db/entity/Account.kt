package com.zeki.mole_tunnel_db.entity

import com.zeki.common.em.TradeMode
import com.zeki.common.entity.BaseEntity
import com.zeki.mole_tunnel_db.dto.KisTokenResDto
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
@Table(
    name = "account"
)
class Account private constructor(
    grantType: String,
    appKey: String,
    appSecret: String,
    accessToken: String,
    tokenType: String,
    expiredIn: Int,
    accessTokenExpired: LocalDateTime,
    accountNumber: String,
    accountType: TradeMode,
    accountName: String
) : BaseEntity() {
    //    PSXPLzUchwGU7KQ48mTqnHK8pANFKeDnVojx
    @Column(name = "grant_type", nullable = false)
    var grantType: String = grantType
        protected set

    @Column(name = "app_key", nullable = false, length = 100)
    var appKey: String = appKey
        protected set

    @Column(name = "app_secret", nullable = false, length = 200)
    var appSecret: String = appSecret
        protected set

    @Column(name = "access_token", nullable = false, length = 255)
    var accessToken: String = accessToken
        protected set

    @Column(name = "token_type", nullable = false, length = 50)
    var tokenType: String = tokenType
        protected set

    @Column(name = "expired_in", nullable = false)
    var expiredIn: Int = expiredIn
        protected set

    @Column(name = "access_token_expired", nullable = false)
    var accessTokenExpired: LocalDateTime = accessTokenExpired
        protected set

    @Column(name = "account_number", nullable = false, length = 50)
    var accountNumber: String = accountNumber
        protected set


    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    var accountType: TradeMode = accountType
        protected set

    @Column(name = "account_name", nullable = false, length = 20)
    var accountName: String = accountName
        protected set

    companion object {
        fun create(
            grantType: String,
            appKey: String,
            appSecret: String,
            accessToken: String,
            tokenType: String,
            expiredIn: Int,
            accessTokenExpired: LocalDateTime,
            accountNumber: String,
            accountType: TradeMode,
            accountName: String
        ): Account {
            return Account(
                grantType = grantType,
                appKey = appKey,
                appSecret = appSecret,
                accessToken = accessToken,
                tokenType = tokenType,
                expiredIn = expiredIn,
                accessTokenExpired = accessTokenExpired,
                accountNumber = accountNumber,
                accountType = accountType,
                accountName = accountName
            )
        }
    }

    fun updateToken(kisTokenResDto: KisTokenResDto) {
        this.tokenType = kisTokenResDto.tokenType
        this.accessToken = kisTokenResDto.accessToken
        this.accessTokenExpired = LocalDateTime.parse(
            kisTokenResDto.accessTokenTokenExpired,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        ).minusHours(1)
    }

    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(accessTokenExpired)
    }
}
