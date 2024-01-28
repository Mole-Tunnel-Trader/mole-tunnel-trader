package com.zeki.kisvolkotlin.domain._common.util

import org.springframework.core.env.Environment

object CustomUtils {
    fun isProdProfile(environment: Environment): Boolean {
        return "prod" in environment.activeProfiles || "prod" in environment.defaultProfiles
    }
}

