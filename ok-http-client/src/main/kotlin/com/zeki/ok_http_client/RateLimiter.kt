package com.zeki.ok_http_client

import com.zeki.common.em.TradeMode
import jakarta.annotation.PostConstruct
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component

@Component
open class RateLimiter(private val cacheManager: CacheManager) {

    private val logger = LoggerFactory.getLogger(RateLimiter::class.java)
    private val cacheName = "kisApiRequests"

    @PostConstruct
    fun init() {
        // 캐시가 존재하는지 확인하고 없으면 생성
        if (cacheManager.getCache(cacheName) == null) {
            throw IllegalStateException("Cache '$cacheName' is not defined in CacheManager")
        }
    }

    open fun getRequestTimes(appkey: String): MutableList<LocalDateTime> {
        val cache = cacheManager.getCache(cacheName)
        @Suppress("UNCHECKED_CAST")
        return cache?.get(appkey, MutableList::class.java) as? MutableList<LocalDateTime>
                ?: mutableListOf<LocalDateTime>().also { cache?.put(appkey, it) }
    }

    open fun waitForRateLimit(appkey: String, tradeMode: TradeMode) {
        val maxRequests =
                when (tradeMode) {
                    TradeMode.TRAIN -> 2
                    TradeMode.REAL, TradeMode.BATCH -> 19 // 1초당 최대 19개 요청 허용
                }

        while (true) {
            val currentTime = LocalDateTime.now()
            val requestTimes = getRequestTimes(appkey)

            // 1초 이내의 유효한 요청만 필터링
            val validRequestTimes =
                    requestTimes
                            .filter { it.until(currentTime, ChronoUnit.MILLIS) < 1000 }
                            .toMutableList()

            // 캐시 업데이트 (필요한 경우)
            if (validRequestTimes.size != requestTimes.size) {
                val cache = cacheManager.getCache(cacheName)
                cache?.put(appkey, validRequestTimes)
                logger.debug("캐시 정리: ${requestTimes.size} -> ${validRequestTimes.size} 요청")
            }

            // 요청 수가 제한 이내인 경우 즉시 처리
            if (validRequestTimes.size < maxRequests) {
                validRequestTimes.add(currentTime)
                val cache = cacheManager.getCache(cacheName)
                cache?.put(appkey, validRequestTimes)
                logger.debug("요청 처리: 현재 ${validRequestTimes.size}/${maxRequests} 요청")
                break
            }

            // 요청 제한에 도달한 경우 대기 시간 계산
            val oldestRequest = validRequestTimes.minOrNull()
            if (oldestRequest != null) {
                val elapsedMillis = oldestRequest.until(currentTime, ChronoUnit.MILLIS)
                // 가장 오래된 요청이 만료될 때까지 대기 (최소 200ms)
                val waitTimeMillis = (1000 - elapsedMillis).coerceAtLeast(200)
                logger.debug(
                        "요청 제한 도달: ${validRequestTimes.size}/${maxRequests}, 대기 시간: ${waitTimeMillis}ms"
                )
                TimeUnit.MILLISECONDS.sleep(waitTimeMillis)
            } else {
                // 유효한 요청이 없는 경우 (이론적으로는 발생하지 않음)
                TimeUnit.MILLISECONDS.sleep(200)
            }
        }
    }
}
