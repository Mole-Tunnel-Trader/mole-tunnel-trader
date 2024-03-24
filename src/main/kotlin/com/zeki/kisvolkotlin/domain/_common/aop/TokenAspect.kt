package com.zeki.kisvolkotlin.domain._common.aop

import com.zeki.kisvolkotlin.domain.kis.token.TokenHolder
import com.zeki.kisvolkotlin.domain.kis.token.TokenService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class TokenAspect(
    private val tokenService: TokenService
) {


    // 경우에 따라 Connection이 두번 발생할 수 있음
    @Around("@annotation(com.zeki.kisvolkotlin.domain._common.aop.Token)")
    fun tokenExecution(joinPoint: ProceedingJoinPoint): Any? {
        val token = tokenService.getOrCreateToken()
        TokenHolder.setToken(token)

        val result = joinPoint.proceed()

        TokenHolder.clear()
        return result
    }

}