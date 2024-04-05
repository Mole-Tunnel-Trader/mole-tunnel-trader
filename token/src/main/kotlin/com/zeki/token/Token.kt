package com.zeki.token

import com.zeki.common.em.TradeMode
import com.zeki.common.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(name = "token")
class Token(
    tokenType: String,
    tokenValue: String,
    tradeMode: TradeMode,
    expiredDate: LocalDateTime
) : BaseEntity() {

    @Column(name = "token_type", nullable = false, length = 20)
    @Comment("토큰 타입")
    var tokenType: String = tokenType
        protected set

    @Column(name = "token_value", nullable = false, length = 1000)
    @Comment("토큰 값")
    var tokenValue: String = tokenValue
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_mode", nullable = false, length = 10)
    @Comment("매매 모드 (REAL, TRAIN)")
    var tradeMode: TradeMode = tradeMode
        protected set

    @Column(name = "expired_date", nullable = false)
    @Comment("만료 일자")
    var expiredDate: LocalDateTime = expiredDate
        protected set

    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiredDate)
    }
}