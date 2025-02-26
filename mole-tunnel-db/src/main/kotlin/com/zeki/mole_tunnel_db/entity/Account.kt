package com.zeki.mole_tunnel_db.entity

import com.zeki.common.em.TradeMode
import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

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
    accountType: TradeMode
) : BaseEntity() {

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
                accountType: TradeMode
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
                accountType = accountType
            )
        }
    }

    fun updateToken(newAccessToken: String, newExpiration: LocalDateTime, newExpiredIn: Int): Boolean {
        return if (accessToken == newAccessToken && accessTokenExpired == newExpiration && expiredIn == newExpiredIn) {
            false
        } else {
            accessToken = newAccessToken
            accessTokenExpired = newExpiration
            expiredIn = newExpiredIn
            true
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is Account && id != null && id == other.id)
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
