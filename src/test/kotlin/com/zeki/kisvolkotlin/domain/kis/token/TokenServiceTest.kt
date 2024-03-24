package com.zeki.kisvolkotlin.domain.kis.token

import com.zeki.kisvolkotlin.db.entity.Token
import com.zeki.kisvolkotlin.db.entity.em.TradeMode
import com.zeki.kisvolkotlin.db.repository.TokenRepository
import com.zeki.kisvolkotlin.domain._common.webclient.ApiStatics
import com.zeki.kisvolkotlin.domain._common.webclient.WebClientConnector
import com.zeki.kisvolkotlin.domain.kis.token.extend.ExtendTokenService

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
class TokenServiceTest(
    @Autowired private var tokenRepository: TokenRepository,
    @Autowired private var apiStatics: ApiStatics,
    @Autowired private var webClientConnector: WebClientConnector,
    @Autowired private var env: Environment,
) {
    private val extendTokenService = ExtendTokenService(tokenRepository, apiStatics, webClientConnector, env)

    @Nested
    @DisplayName("성공 테스트")
    inner class Success {

        @Test
        fun `getOrCreateToken() - 토큰 생성`() {
            // given
            Token(
                tokenValue = "test",
                tradeMode = TradeMode.TRAIN,
                expiredDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0)
            )

            // when
            val token = extendTokenService.getOrCreateToken()


            // then


        }
    }
}