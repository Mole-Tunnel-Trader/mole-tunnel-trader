package com.zeki.kisserver

import com.github.benmanes.caffeine.cache.Caffeine
import com.zeki.common.em.TradeMode
import com.zeki.ok_http_client.RateLimiter
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager

class RateLimiterTest : BehaviorSpec() {
    // SpringExtension 추가
    override fun extensions(): List<Extension> = listOf(SpringExtension)

    init {
        // 테스트에 필요한 객체 생성
        val cacheManager =
                CaffeineCacheManager().apply {
                    setCaffeine(
                            Caffeine.newBuilder()
                                    .expireAfterWrite(1, TimeUnit.SECONDS)
                                    .maximumSize(1000)
                    )
                    setCacheNames(listOf("kisApiRequests"))
                }

        // RateLimiter를 상속받는 테스트용 클래스 구현
        class TestRateLimiter(private val cacheManager: CacheManager) : RateLimiter() {
            override fun getRequestTimes(appkey: String): MutableList<LocalDateTime> {
                val cache = cacheManager.getCache("kisApiRequests")
                @Suppress("UNCHECKED_CAST")
                return cache?.get(appkey, MutableList::class.java) as? MutableList<LocalDateTime>
                        ?: mutableListOf<LocalDateTime>().also { cache?.put(appkey, it) }
            }
        }

        val rateLimiter = TestRateLimiter(cacheManager)

        Given("KIS API 요청 제한 관리 시스템") {
            val appkey = "testAppKey"

            When("TRAIN 모드에서 요청 제한 이내로 API 호출 시") {
                val accountType = TradeMode.TRAIN

                Then("요청이 즉시 처리된다") {
                    // 첫 번째 요청
                    val startTime1 = System.currentTimeMillis()
                    rateLimiter.waitForRateLimit(appkey, accountType)
                    val endTime1 = System.currentTimeMillis()

                    // 두 번째 요청
                    val startTime2 = System.currentTimeMillis()
                    rateLimiter.waitForRateLimit(appkey, accountType)
                    val endTime2 = System.currentTimeMillis()

                    // 각 요청이 빠르게 처리되었는지 확인 (50ms 이내)
                    (endTime1 - startTime1).shouldBeLessThan(50)
                    (endTime2 - startTime2).shouldBeLessThan(50)
                }
            }

            When("TRAIN 모드에서 요청 제한을 초과하여 API 호출 시") {
                val accountType = TradeMode.TRAIN
                val newAppkey = "trainAppKey2" // 새로운 키 사용

                Then("세 번째 요청은 대기 후 처리된다") {
                    // 첫 번째 요청
                    rateLimiter.waitForRateLimit(newAppkey, accountType)

                    // 두 번째 요청
                    rateLimiter.waitForRateLimit(newAppkey, accountType)

                    // 세 번째 요청 (대기 발생)
                    val startTime = System.currentTimeMillis()
                    rateLimiter.waitForRateLimit(newAppkey, accountType)
                    val endTime = System.currentTimeMillis()

                    // 세 번째 요청이 대기 후 처리되었는지 확인 (최소 100ms 이상 대기)
                    (endTime - startTime).shouldBeGreaterThanOrEqual(100)
                }
            }

            When("REAL 모드에서 요청 제한 이내로 API 호출 시") {
                val accountType = TradeMode.REAL
                val maxRequests = 19
                val newAppkey = "realAppKey"

                Then("모든 요청이 즉시 처리된다") {
                    // 19개 요청 모두 빠르게 처리되는지 확인
                    val startTime = System.currentTimeMillis()

                    repeat(maxRequests) { rateLimiter.waitForRateLimit(newAppkey, accountType) }

                    val endTime = System.currentTimeMillis()

                    // 모든 요청이 합리적인 시간 내에 처리되었는지 확인 (1초 이내)
                    (endTime - startTime).shouldBeLessThan(1000)
                }
            }

            When("REAL 모드에서 요청 제한을 초과하여 API 호출 시") {
                val accountType = TradeMode.REAL
                val maxRequests = 19
                val newAppkey = "realAppKey2"

                Then("초과 요청은 대기 후 처리된다") {
                    // 19개 요청 모두 처리
                    repeat(maxRequests) { rateLimiter.waitForRateLimit(newAppkey, accountType) }

                    // 20번째 요청 (대기 발생)
                    val startTime = System.currentTimeMillis()
                    rateLimiter.waitForRateLimit(newAppkey, accountType)
                    val endTime = System.currentTimeMillis()

                    // 초과 요청이 대기 후 처리되었는지 확인 (최소 100ms 이상 대기)
                    (endTime - startTime).shouldBeGreaterThanOrEqual(100)
                }
            }

            When("TTL 캐시 만료 후 API 호출 시") {
                val accountType = TradeMode.TRAIN
                val newAppkey = "expiredAppKey"

                Then("요청 제한이 초기화되어 즉시 처리된다") {
                    // 첫 번째 요청
                    rateLimiter.waitForRateLimit(newAppkey, accountType)

                    // 두 번째 요청
                    rateLimiter.waitForRateLimit(newAppkey, accountType)

                    // 1초 이상 대기 (캐시 만료)
                    TimeUnit.SECONDS.sleep(1)

                    // 캐시 만료 후 요청
                    val startTime = System.currentTimeMillis()
                    rateLimiter.waitForRateLimit(newAppkey, accountType)
                    val endTime = System.currentTimeMillis()

                    // 요청이 즉시 처리되었는지 확인 (50ms 이내)
                    (endTime - startTime).shouldBeLessThan(50)
                }
            }
        }
    }
}
