package com.zeki.kisvolkotlin.exception

data class ApiException(
    val responseCode: ResponseCode,
    val messages: String = responseCode.defaultMessage
) : RuntimeException()
