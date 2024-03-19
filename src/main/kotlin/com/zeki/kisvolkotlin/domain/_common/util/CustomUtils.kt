package com.zeki.kisvolkotlin.domain._common.util

import org.springframework.core.env.Environment
import java.time.LocalDate

object CustomUtils {
    fun isProdProfile(environment: Environment): Boolean {
        return "prod" in environment.activeProfiles || "prod" in environment.defaultProfiles
    }

    fun Int.toLocalDate(): LocalDate {
        return LocalDate.of(
            this / 10000,
            this / 100 % 100,
            this % 100
        )
    }
}

