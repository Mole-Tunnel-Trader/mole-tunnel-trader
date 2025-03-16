package com.zeki.ok_http_client

import com.zeki.common.em.TradeMode
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
open class RateLimiter {

    @Cacheable(value = ["kisApiRequests"], key = "#appkey")
    open fun getRequestTimes(appkey: String): MutableList<LocalDateTime> {
        return mutableListOf()
    }

    open fun waitForRateLimit(appkey: String, tradeMode: TradeMode) {
        while (true) {
            val currentTime = LocalDateTime.now()
            val requestTimes = getRequestTimes(appkey)

            // TTL 캐시를 사용하므로 1초 이전 요청은 자동으로 제거됨
            // 하지만 안전을 위해 1초 이전 요청 제거 로직 유지
            requestTimes.removeIf { it.isBefore(currentTime.minusSeconds(1)) }

            val maxRequests =
                    when (tradeMode) {
                        TradeMode.TRAIN -> 2
                        TradeMode.REAL, TradeMode.BATCH -> 19
                    }

            if (requestTimes.size < maxRequests) {
                requestTimes.add(currentTime)
                break
            }

            // 가장 오래된 요청 시간을 확인하여 대기 시간 계산
            val oldestRequest = requestTimes.minOrNull()
            if (oldestRequest != null) {
                val waitTimeMillis =
                        (1000 -
                                        oldestRequest.until(
                                                currentTime,
                                                java.time.temporal.ChronoUnit.MILLIS
                                        ))
                                .coerceAtLeast(100)
                TimeUnit.MILLISECONDS.sleep(waitTimeMillis)
            }
        }
    }
}
