package com.zeki.kisserver.domain.kis.token

import com.zeki.kisserver.exception.ResponseCode
import com.zeki.kisvolkotlin.exception.ResponseCode

object TokenHolder {
    private val tokenThreadLocal = ThreadLocal<com.zeki.kisserver.db.entity.Token>()

    fun setToken(token: com.zeki.kisserver.db.entity.Token) {
        tokenThreadLocal.set(token)
    }

    // ThreadLocal 사용시 get 함수는 항상 변수를 쓰레드별로 복사해서 사용함
    fun getToken(): com.zeki.kisserver.db.entity.Token =
        tokenThreadLocal.get() ?: throw com.zeki.kisserver.exception.ApiException(
            ResponseCode.INTERNAL_SERVER_ERROR,
            "Token이 세팅되기 전에 접근함"
        )

    // 메모리 누수 가능성이 있어 항상 remove 해줘야함
    fun clear() {
        tokenThreadLocal.remove()
    }
}