package com.zeki.kisserver.domain.kis.account

import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
        name = "account",
        indexes = [Index(name = "idx_user_id", columnList = "user_id")],
        uniqueConstraints = [
            UniqueConstraint(columnNames = ["user_id"]),
            UniqueConstraint(columnNames = ["account_number"])
        ]
)
class Account(
        grantType: String,
        appKey: String,
        appSecret: String,
        accessToken: String,
        tokenType: String,
        expiredIn: Int,
        accessTokenExpired: LocalDateTime,
        accountNumber: String,
        accountType: String,
        userId: String,
        password: String
) : BaseEntity() {

    //  protected set을 쓰면 해당 속성은 읽기는 가능하지만, 외부에서 변경할 수 없음.
    //  대신 엔티티 내부에서만 변경할 수 있기 때문에 일관성을 유지할 수 있음.
    @Column(name = "user_id", nullable = false, length = 100)
    var userId: String = userId
        protected set

    @Column(name = "password", nullable = false, length = 255)
    var password: String = password
        protected set

    @Column(name = "grant_type", nullable = false)
    var grantType: String = grantType
        protected set

    @Column(name = "app_key", nullable = false, length = 100)
    var appKey: String = appKey
        protected set

    @Column(name = "app_secret", nullable = false, length = 100)
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

    @Column(name = "account_type", nullable = false, length = 20)
    var accountType: String = accountType
        protected set

    // 토큰 정보 업데이트 메서드
    fun updateToken(newAccessToken: String, newExpiration: LocalDateTime, newExpiredIn: Int): Boolean {
        if (!checkTokenUpdateRequired(newAccessToken, newExpiration, newExpiredIn)) return false
        applyTokenUpdate(newAccessToken, newExpiration, newExpiredIn)
        return true
    }

    // 토큰 업데이트가 필요한지 확인하는 메서드
    private fun checkTokenUpdateRequired(newAccessToken: String, newExpiration: LocalDateTime, newExpiredIn: Int): Boolean {
        return this.accessToken != newAccessToken ||
                this.accessTokenExpired != newExpiration ||
                this.expiredIn != newExpiredIn
    }

    // 토큰을 업데이트하는 메서드
    private fun applyTokenUpdate(newAccessToken: String, newExpiration: LocalDateTime, newExpiredIn: Int) {
        this.accessToken = newAccessToken
        this.accessTokenExpired = newExpiration
        this.expiredIn = newExpiredIn
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false
        return id != null && id == other.id
    }

    // 동일한 엔티티를 올바르게 비교하고, Set/Map에서 중복을 방지하기 위해 필요함
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
