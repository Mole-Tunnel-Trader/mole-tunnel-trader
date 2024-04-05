package com.zeki.trade.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class KisOrderStockResDto(
    @JsonProperty("msg1")
    val msg1: String,
    @JsonProperty("msg_cd")
    val msgCd: String,
    @JsonProperty("output")
    val output: Output,
    @JsonProperty("rt_cd")
    val rtCd: String
) {
    data class Output(
        @JsonProperty("KRX_FWDG_ORD_ORGNO")
        val kRXFWDGORDORGNO: String,
        @JsonProperty("ODNO")
        val oDNO: String,
        @JsonProperty("ORD_TMD")
        val oRDTMD: String
    )
}