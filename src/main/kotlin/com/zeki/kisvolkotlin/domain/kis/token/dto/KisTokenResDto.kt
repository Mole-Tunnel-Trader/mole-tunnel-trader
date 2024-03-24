package com.zeki.kisvolkotlin.domain.kis.token.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class KisTokenResDto(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("access_token_token_expired")
    val accessTokenTokenExpired: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("token_type")
    val tokenType: String
)