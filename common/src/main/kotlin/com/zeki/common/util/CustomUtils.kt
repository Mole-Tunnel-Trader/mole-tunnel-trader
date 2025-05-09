package com.zeki.common.util

import com.zeki.common.em.TradeMode
import org.springframework.core.env.Environment
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object CustomUtils {
    fun isProdProfile(environment: Environment): Boolean =
        true

    fun nowTradeMode(environment: Environment): TradeMode = TradeMode.REAL

    /**
     * 일봉 기준이므로
     * 16:00 이전이라면 전일 날짜를 반환하기 위해 만들었다.
     * 현재는 기준시간이 16:00
     */
    fun getStandardNowDate(): LocalTime =
        LocalTime.of(16, 0)


    fun Int.toLocalDate(format: String = "yyyyMMdd"): LocalDate =
        LocalDate.parse(this.toString(), DateTimeFormatter.ofPattern(format))

    fun LocalDate.toStringDate(format: String = "yyyyMMdd"): String =
        this.format(DateTimeFormatter.ofPattern(format))

    fun String.toLocalDate(format: String = "yyyyMMdd"): LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ofPattern(format))
}

// BigDecimal 리스트의 평균을 계산하는 확장 함수
fun List<BigDecimal>.average(): BigDecimal {
    if (this.isEmpty()) return BigDecimal.ZERO
    val sum = this.fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }
    return sum.divide(BigDecimal.valueOf(this.size.toLong()), 10, RoundingMode.HALF_UP)
}

// BigDecimal 리스트의 합을 계산하는 확장 함수 (성능 개선)
fun List<BigDecimal>.sum(): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }
}
