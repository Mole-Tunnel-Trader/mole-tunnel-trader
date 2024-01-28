package com.zeki.kisvolkotlin.domain._common.controller

import com.zeki.kisvolkotlin.domain._common.dto.CommonResDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@Tag(name = "00. HealthCheck", description = "서버 상태 체크")
class HealthCheckController {

    @GetMapping("")
    fun healthCheck(): CommonResDto<String> {
        return CommonResDto.success(
            LocalDateTime.now().toString()
        )
    }
}