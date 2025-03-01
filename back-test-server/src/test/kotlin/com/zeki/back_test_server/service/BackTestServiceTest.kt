package com.zeki.back_test_server.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@Transactional
class BackTestServiceTest(
    @Autowired
    private val backTestService: BackTestService
) {

    @Test
    fun backTest() {
        backTestService.backTest(
            1L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2025, 1, 1),
            BigDecimal(1_000_000)
        )
    }
}