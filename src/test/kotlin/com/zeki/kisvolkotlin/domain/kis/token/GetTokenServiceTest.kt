package com.zeki.kisvolkotlin.domain.kis.token

import com.zeki.kisvolkotlin.db.entity.Token
import com.zeki.kisvolkotlin.db.entity.em.TradeMode
import com.zeki.kisvolkotlin.db.repository.TokenRepository
import com.zeki.kisvolkotlin.domain._common.webclient.ApiStatics
import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import com.zeki.kisvolkotlin.domain.kis.token.extend.ExtendTokenService
import com.zeki.kisvolkotlin.exception.ApiException
import com.zeki.kisvolkotlin.exception.ResponseCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Suppress("LocalVariableName")
class GetTokenServiceTest(
    @Autowired private var tokenRepository: TokenRepository,
    @Autowired private var apiStatics: ApiStatics,
    @Autowired private var webClientConnector: WebClientConnector,
    @Autowired private var env: Environment,
) {

    private var extendTokenService: ExtendTokenService =
        ExtendTokenService(tokenRepository, apiStatics, webClientConnector, env)

    @Nested
    @DisplayName("성공 테스트")
    inner class Success {

        @Test
        fun `getOrCreateToken() - 토큰 생성`() {
            // given
            // when
            val token = extendTokenService.getOrCreateToken()

            // then
            assertEquals(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6IjVlOWExYzlhLWRhYWUtNDRhZS04NzI3LTk2Y2EwNzAzYWY0NCIsImlzcyI6InVub2d3IiwiZXhwIjoxNzExMzM3OTY3LCJpYXQiOjE3MTEyNTE1NjcsImp0aSI6IlBTc0lCT1hXZ0duNloyRER2Qm1zWm5oeURKNU5mdGpCdll0aCJ9.DsRRy6F__oPARyOzvu0FQ4BFAnTpnbHI7eqDTQ3jr1bNLR6SImNXNvmd7NRojwGH35mhy9OOJHZxmUpclwW4zA",
                token.tokenValue
            )

        }

        @Test
        fun `getOrCreateToken() - 토큰 재사용`() {
            // given
            val _token = Token(
                tokenType = "test",
                tokenValue = "token",
                tradeMode = TradeMode.TRAIN,
                expiredDate = LocalDateTime.now().plusDays(1)
            )
            tokenRepository.save(_token)

            // when
            val token1 = extendTokenService.getOrCreateToken()
            val token2 = extendTokenService.getOrCreateToken()

            // then
            assertAll(
                { assertEquals("token", token1.tokenValue) },
                { assertEquals("token", token2.tokenValue) },
            )
        }

        @Test
        fun `checkToken() - 토큰 만료시 재생성`() {
            // given
            val _token = Token(
                tokenType = "test",
                tokenValue = "token",
                tradeMode = TradeMode.TRAIN,
                expiredDate = LocalDateTime.now().minusDays(1)
            )
            tokenRepository.save(_token)

            // when
            val token = extendTokenService.getOrCreateToken()

            // then
            assertEquals(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6IjVlOWExYzlhLWRhYWUtNDRhZS04NzI3LTk2Y2EwNzAzYWY0NCIsImlzcyI6InVub2d3IiwiZXhwIjoxNzExMzM3OTY3LCJpYXQiOjE3MTEyNTE1NjcsImp0aSI6IlBTc0lCT1hXZ0duNloyRER2Qm1zWm5oeURKNU5mdGpCdll0aCJ9.DsRRy6F__oPARyOzvu0FQ4BFAnTpnbHI7eqDTQ3jr1bNLR6SImNXNvmd7NRojwGH35mhy9OOJHZxmUpclwW4zA",
                token.tokenValue
            )
        }

    }

    @Nested
    @DisplayName("실패 테스트")
    inner class FailTest {

        @Test
        fun `getOrCreateToken() - 토큰이 생성되기전에 접근`() {
            // given
            TokenHolder.clear()

            // when
            val thrownException = assertThrows(ApiException::class.java) {
                TokenHolder.getToken()
            }

            // then
            assertAll(
                { assertEquals("Token이 세팅되기 전에 접근함", thrownException.messages) },
                { assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, thrownException.responseCode) }
            )
        }

    }

}