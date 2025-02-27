package com.zeki.kisserver

import com.zeki.common.em.TradeMode
import com.zeki.mole_tunnel_db.entity.Account
import com.zeki.mole_tunnel_db.repository.AccountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
//@Commit  // 롤백 방지 (테스트 실행 후 데이터가 DB에 반영됨)
class CreateInitAccountTest @Autowired constructor(
        val accountRepository: AccountRepository
) {

    @Test
    fun `Account 저장 및 조회 테스트`() {
        // Given
        val account = Account.create(
                grantType = "client_credentials",
                appKey = "testAppKey",
                appSecret = "testAppSecret",
                accessToken = "",
                tokenType = "",
                expiredIn = 0,
                accessTokenExpired = LocalDateTime.now().plusHours(6),
                accountNumber = "",
                accountType = TradeMode.TRAIN,
                accountName = "test"
        )

        // When
        val savedAccount = accountRepository.save(account)
        val foundAccount = accountRepository.findById(savedAccount.id!!).orElse(null)

        // Then
        assertNotNull(foundAccount)
        assertEquals(savedAccount.accountNumber, foundAccount?.accountNumber)
    }
}
