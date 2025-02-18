package com.zeki.common.controller

import com.zeki.common.dto.CommonResDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class HealthCheckController {

    @GetMapping("")
    fun healthCheck(): CommonResDto<String> {

        return CommonResDto.success(
            LocalDateTime.now().toString()
        )
    }

    @GetMapping("/favicon.ico")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun favicon() {
    }
}