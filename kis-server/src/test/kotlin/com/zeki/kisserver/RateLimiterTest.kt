package com.zeki.kisserver

import com.github.benmanes.caffeine.cache.Caffeine
import com.zeki.common.em.TradeMode
import com.zeki.ok_http_client.RateLimiter
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.cache.caffeine.CaffeineCacheManager

class RateLimiterTest {

    private val cacheManager =
            CaffeineCacheManager().apply {
                setCaffeine(
                        Caffeine.newBuilder()
                                .expireAfterWrite(2, TimeUnit.SECONDS)
                                .maximumSize(1000)
                )
                setCacheNames(listOf("kisApiRequests"))
            }

    private val rateLimiter = RateLimiter(cacheManager)

    @Test
    fun `TRAIN 모드에서 요청 제한 이내로 API 호출 시 즉시 처리된다`() {
        val appkey = "testAppKey"
        val accountType = TradeMode.TRAIN

        // 첫 번째 요청
        val startTime1 = System.currentTimeMillis()
        rateLimiter.waitForRateLimit(appkey, accountType)
        val endTime1 = System.currentTimeMillis()

        // 두 번째 요청
        val startTime2 = System.currentTimeMillis()
        rateLimiter.waitForRateLimit(appkey, accountType)
        val endTime2 = System.currentTimeMillis()

        // 각 요청이 빠르게 처리되었는지 확인 (200ms 이내로 변경)
        assertTrue(endTime1 - startTime1 < 200, "첫 번째 요청이 200ms 이내에 처리되어야 합니다.")
        assertTrue(endTime2 - startTime2 < 200, "두 번째 요청이 200ms 이내에 처리되어야 합니다.")
    }

    @Test
    fun `TRAIN 모드에서 요청 제한을 초과하여 API 호출 시 대기 후 처리된다`() {
        val accountType = TradeMode.TRAIN
        val newAppkey = "trainAppKey2" // 새로운 키 사용

        // 첫 번째 요청
        rateLimiter.waitForRateLimit(newAppkey, accountType)

        // 두 번째 요청
        rateLimiter.waitForRateLimit(newAppkey, accountType)

        // 세 번째 요청 (대기 발생)
        val startTime = System.currentTimeMillis()
        rateLimiter.waitForRateLimit(newAppkey, accountType)
        val endTime = System.currentTimeMillis()

        // 세 번째 요청이 대기 후 처리되었는지 확인 (최소 200ms 이상 대기)
        assertTrue(endTime - startTime >= 200, "세 번째 요청은 최소 200ms 이상 대기해야 합니다.")
    }

    @Test
    fun `REAL 모드에서 요청 제한 이내로 API 호출 시 모든 요청이 즉시 처리된다`() {
        val accountType = TradeMode.REAL
        val maxRequests = 19
        val newAppkey = "realAppKey"

        // 19개 요청 모두 빠르게 처리되는지 확인
        val startTime = System.currentTimeMillis()

        repeat(maxRequests) { rateLimiter.waitForRateLimit(newAppkey, accountType) }

        val endTime = System.currentTimeMillis()

        // 모든 요청이 합리적인 시간 내에 처리되었는지 확인 (3초 이내로 유지)
        assertTrue(endTime - startTime < 3000, "모든 요청이 3초 이내에 처리되어야 합니다.")
    }

    @Test
    fun `REAL 모드에서 요청 제한을 초과하여 API 호출 시 초과 요청은 대기 후 처리된다`() {
        val accountType = TradeMode.REAL
        val maxRequests = 19
        val newAppkey = "realAppKey2"

        // 19개 요청 모두 처리
        repeat(maxRequests) { rateLimiter.waitForRateLimit(newAppkey, accountType) }

        // 20번째 요청 (대기 발생)
        val startTime = System.currentTimeMillis()
        rateLimiter.waitForRateLimit(newAppkey, accountType)
        val endTime = System.currentTimeMillis()

        // 초과 요청이 대기 후 처리되었는지 확인 (최소 200ms 이상 대기)
        assertTrue(endTime - startTime >= 200, "초과 요청은 최소 200ms 이상 대기해야 합니다.")
    }

    @Test
    fun `TTL 캐시 만료 후 API 호출 시 요청 제한이 초기화되어 즉시 처리된다`() {
        val accountType = TradeMode.TRAIN
        val newAppkey = "expiredAppKey"

        // 첫 번째 요청
        rateLimiter.waitForRateLimit(newAppkey, accountType)

        // 두 번째 요청
        rateLimiter.waitForRateLimit(newAppkey, accountType)

        // 2.5초 이상 대기 (캐시 만료)
        TimeUnit.MILLISECONDS.sleep(2500)

        // 캐시 만료 후 요청
        val startTime = System.currentTimeMillis()
        rateLimiter.waitForRateLimit(newAppkey, accountType)
        val endTime = System.currentTimeMillis()

        // 요청이 즉시 처리되었는지 확인 (200ms 이내로 변경)
        assertTrue(endTime - startTime < 200, "캐시 만료 후 요청이 200ms 이내에 처리되어야 합니다.")
    }
}
