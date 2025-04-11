package com.zeki.kisserver.domain.schduler

import com.zeki.holiday.service.HolidayDateService
import com.zeki.holiday.service.HolidayService
import com.zeki.stockcode.service.GetStockCodeService
import com.zeki.stockdata.service.stock_price.StockPriceService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AsyncScheduler(
    private val getStockCodeService: GetStockCodeService,
    private val stockPriceService: StockPriceService,
    private val holidayService: HolidayService,
    private val holidayDateService: HolidayDateService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Async
    fun updateRsi(now: LocalDate) {
        val stockCodeList = getStockCodeService.getStockCodeStringList()
        logger.info("RSI 업데이트 시작: 종목 수 = ${stockCodeList.size}")
        stockPriceService.updateRsi(stockCodeList, now)
        logger.info("RSI 업데이트 완료")
    }

    @Async
    fun updateVolumeIndicators(now: LocalDate, count: Int = 10) {
        val stockCodeList = getStockCodeService.getStockCodeStringList()
        logger.info("볼륨 관련 지표 업데이트 시작: 날짜 = $now, 종목 수 = ${stockCodeList.size}")

        try {
            // 최대 처리할 과거 날짜
            val maxDaysToProcess = count
            var successCount = 0
            var totalUpdated = 0

            // 대용량 데이터 처리를 위한 청크 사이즈 정의
            val chunkSize = 500

            // 종목 코드를 청크로 분할
            val stockCodeChunks = stockCodeList.chunked(chunkSize)
            logger.info("종목을 ${stockCodeChunks.size}개 그룹으로 나누어 처리합니다 (청크 사이즈: $chunkSize)")

            // 날짜별 처리
            for (i in 0 until maxDaysToProcess) {
                val targetDate = now.minusDays(i.toLong())

                // 주말과 공휴일 건너뛰기
                if (isHolidayOrWeekend(targetDate)) {
                    logger.info("${targetDate}은(는) 주말 또는 공휴일이므로 처리를 건너뜁니다")
                    continue
                }

                logger.info("${targetDate} 날짜 데이터 처리 시작")

                var dateUpdatedCount = 0
                val startTime = System.currentTimeMillis()

                // 각 청크별로 순차 처리
                stockCodeChunks.forEachIndexed { chunkIndex, chunk ->
                    logger.info(
                        "청크 #${chunkIndex + 1}/${stockCodeChunks.size}: ${targetDate} 날짜 처리 시작 (${chunk.size}개 종목)"
                    )
                    val chunkStartTime = System.currentTimeMillis()

                    try {
                        val updatedCount =
                            stockPriceService.updateVolumeIndicators(chunk, targetDate)
                        dateUpdatedCount += updatedCount

                        val chunkElapsedTime = System.currentTimeMillis() - chunkStartTime
                        logger.info(
                            "청크 #${chunkIndex + 1}: ${targetDate} 날짜 처리 완료 (${chunk.size}개 종목), " +
                                    "$updatedCount 개 업데이트, 처리 시간: ${chunkElapsedTime}ms"
                        )
                    } catch (e: Exception) {
                        logger.error(
                            "청크 #${chunkIndex + 1}: ${targetDate} 날짜 처리 중 오류 발생: ${e.message}",
                            e
                        )
                    }
                }

                totalUpdated += dateUpdatedCount
                val elapsedTime = System.currentTimeMillis() - startTime
                logger.info(
                    "${targetDate} 날짜 처리 완료: $dateUpdatedCount 개 데이터 업데이트됨, 처리 시간: ${elapsedTime}ms"
                )
                successCount++
            }

            logger.info("볼륨 관련 지표 업데이트 완료: $successCount 일자, 총 $totalUpdated 개 데이터 업데이트됨")
        } catch (e: Exception) {
            logger.error("볼륨 관련 지표 업데이트 중 오류 발생: ${e.message}", e)
        }
    }

    /** 주말이나 공휴일인지 확인하는 메서드 */
    private fun isHolidayOrWeekend(date: LocalDate): Boolean {
        // 토요일(6) 또는 일요일(7)인 경우
        val dayOfWeek = date.dayOfWeek.value
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            return true
        }

        // 공휴일인 경우
        return holidayDateService.isHoliday(date)
    }
}
