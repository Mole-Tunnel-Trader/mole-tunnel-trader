package com.zeki.kisserver.domain._common.aop

import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.mole_tunnel_db.entity.Token

object TokenHolder {
    private val tokenThreadLocal = ThreadLocal<Token>()

    fun setToken(token: Token) {
        tokenThreadLocal.set(token)
    }

    // ThreadLocal 사용시 get 함수는 항상 변수를 쓰레드별로 복사해서 사용함
    fun getToken(): Token =
        tokenThreadLocal.get() ?: throw ApiException(
            ResponseCode.INTERNAL_SERVER_ERROR,
            "Token이 세팅되기 전에 접근함"
        )

    // 메모리 누수 가능성이 있어 항상 remove 해줘야함
    fun clear() {
        tokenThreadLocal.remove()
    }
}