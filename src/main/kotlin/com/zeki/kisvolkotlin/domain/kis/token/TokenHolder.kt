package com.zeki.kisvolkotlin.domain.kis.token

import com.zeki.kisvolkotlin.db.entity.Token
import com.zeki.kisvolkotlin.exception.ApiException
import com.zeki.kisvolkotlin.exception.ResponseCode

object TokenHolder {
    private val tokenThreadLocal = ThreadLocal<Token>()

    fun setToken(token: Token) {
        tokenThreadLocal.set(token)
    }

    // ThreadLocal 사용시 get 함수는 항상 변수를 쓰레드별로 복사해서 사용함
    fun getToken(): Token =
        tokenThreadLocal.get() ?: throw ApiException(ResponseCode.INTERNAL_SERVER_ERROR, "Token이 세팅되기 전에 접근함")

    // 메모리 누수 가능성이 있어 항상 remove 해줘야함
    fun clear() {
        tokenThreadLocal.remove()
    }
}