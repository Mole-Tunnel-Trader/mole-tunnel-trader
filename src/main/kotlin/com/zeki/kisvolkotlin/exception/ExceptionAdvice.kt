package com.zeki.kisvolkotlin.exception

import com.zeki.kisvolkotlin.domain._common.dto.CommonResDto
import com.zeki.kisvolkotlin.domain._common.util.CustomUtils
import org.springframework.core.env.Environment
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class ExceptionAdvice(
    private val environment: Environment
) : Exception() {

    /**
     * 커스텀 예외처리 함수
     */
    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<CommonResDto<Any>> {

        ExceptionUtils.logWarn(e)
        this.printStackTraceNotProd(e)

        return ResponseEntity.status(e.responseCode.status)
            .body(
                CommonResDto.error(
                    e.responseCode.code,
                    e.messages
                )
            )
    }

    /**
     * DB 데이터 중복 예외처리 함수
     */
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(e: DataIntegrityViolationException): ResponseEntity<CommonResDto<Any>> {

        ExceptionUtils.logWarn(e)
        this.printStackTraceNotProd(e)

        return ResponseEntity.status(ResponseCode.CONFLICT_DATA.status)
            .body(
                CommonResDto.error(
                    ResponseCode.CONFLICT_DATA.code,
                    ResponseCode.CONFLICT_DATA.defaultMessage
                )
            )
    }

    /**
     * 모든 예외를 처리하는 함수
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<CommonResDto<Any>> {

        ExceptionUtils.logError(e)
        e.printStackTrace() // 의도하지 못한 예외는 모두 출력

        return ResponseEntity.status(ResponseCode.INTERNAL_SERVER_ERROR.status)
            .body(
                CommonResDto.error(
                    ResponseCode.INTERNAL_SERVER_ERROR.code,
                    ResponseCode.INTERNAL_SERVER_ERROR.defaultMessage
                )
            )
    }

    /**
     * 운영 환경이 아닐 경우에만 StackTrace 출력
     */
    private fun printStackTraceNotProd(e: Exception) {
        if (!CustomUtils.isProdProfile(environment)) {
            e.printStackTrace()
        }
    }
}
