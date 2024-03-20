package com.zeki.kisvolkotlin.domain._common.util

import org.springframework.core.env.Environment
import java.time.LocalDate
import java.time.LocalTime

object CustomUtils {
    fun isProdProfile(environment: Environment): Boolean {
        return "prod" in environment.activeProfiles || "prod" in environment.defaultProfiles
    }

    /**
     * 일봉 기준이므로
     * 16:00 이전이라면 전일 날짜를 반환하기 위해 만들었다.
     * 현재는 기준시간이 16:00
     */
    fun getStandardNowDate(): LocalTime {
        return LocalTime.of(16, 0)
    }

    fun Int.toLocalDate(): LocalDate {
        return LocalDate.of(
            this / 10000,
            this / 100 % 100,
            this % 100
        )
    }
}

