package com.zeki.kisvolkotlin.domain._common.util

import com.zeki.kisvolkotlin.db.entity.em.TradeMode
import org.springframework.core.env.Environment
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object CustomUtils {
    fun isProdProfile(environment: Environment): Boolean =
        "prod" in environment.activeProfiles || "prod" in environment.defaultProfiles

    fun nowTradeMode(environment: Environment): TradeMode =
        if (isProdProfile(environment)) TradeMode.REAL else TradeMode.TRAIN

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

