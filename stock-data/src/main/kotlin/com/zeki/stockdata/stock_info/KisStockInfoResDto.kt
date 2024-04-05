package com.zeki.stockdata.stock_info

import com.fasterxml.jackson.annotation.JsonProperty


data class KisStockInfoResDto(
    @JsonProperty("msg1")
    val msg1: String,
    @JsonProperty("msg_cd")
    val msgCd: String,
    @JsonProperty("output1")
    val output1: Output1,
    @JsonProperty("output2")
    val output2: List<Output2>,
    @JsonProperty("rt_cd")
    val rtCd: String
) {
    data class Output1(
        @JsonProperty("acml_tr_pbmn")
        val acmlTrPbmn: String,
        @JsonProperty("acml_vol")
        val acmlVol: String,
        @JsonProperty("askp")
        val askp: String,
        @JsonProperty("bidp")
        val bidp: String,
        @JsonProperty("cpfn")
        val cpfn: String,
        @JsonProperty("eps")
        val eps: String,
        @JsonProperty("hts_avls")
        val htsAvls: String,
        @JsonProperty("hts_kor_isnm")
        val stockName: String,
        @JsonProperty("itewhol_loan_rmnd_ratem name")
        val itewholLoanRmndRatemName: String,
        @JsonProperty("lstn_stcn")
        val lstnStcn: String,
        @JsonProperty("pbr")
        val pbr: String,
        @JsonProperty("per")
        val per: String,
        @JsonProperty("prdy_ctrt")
        val prdyCtrt: String,
        @JsonProperty("prdy_vol")
        val prdyVol: String,
        @JsonProperty("prdy_vrss")
        val prdyVrss: String,
        @JsonProperty("prdy_vrss_sign")
        val prdyVrssSign: String,
        @JsonProperty("prdy_vrss_vol")
        val prdyVrssVol: String,
        @JsonProperty("stck_fcam")
        val stckFcam: String,
        @JsonProperty("stck_hgpr")
        val stckHgpr: String,
        @JsonProperty("stck_llam")
        val stckLlam: String,
        @JsonProperty("stck_lwpr")
        val stckLwpr: String,
        @JsonProperty("stck_mxpr")
        val stckMxpr: String,
        @JsonProperty("stck_oprc")
        val stckOprc: String,
        @JsonProperty("stck_prdy_clpr")
        val stckPrdyClpr: String,
        @JsonProperty("stck_prdy_hgpr")
        val stckPrdyHgpr: String,
        @JsonProperty("stck_prdy_lwpr")
        val stckPrdyLwpr: String,
        @JsonProperty("stck_prdy_oprc")
        val stckPrdyOprc: String,
        @JsonProperty("stck_prpr")
        val stckPrpr: String,
        @JsonProperty("stck_shrn_iscd")
        val stockCode: String,
        @JsonProperty("vol_tnrt")
        val volTnrt: String
    )

    data class Output2(
        @JsonProperty("acml_tr_pbmn")
        val acmlTrPbmn: String,
        @JsonProperty("acml_vol")
        val acmlVol: String,
        @JsonProperty("flng_cls_code")
        val flngClsCode: String,
        @JsonProperty("mod_yn")
        val modYn: String,
        @JsonProperty("prdy_vrss")
        val prdyVrss: String,
        @JsonProperty("prdy_vrss_sign")
        val prdyVrssSign: String,
        @JsonProperty("prtt_rate")
        val prttRate: String,
        @JsonProperty("revl_issu_reas")
        val revlIssuReas: String,
        @JsonProperty("stck_bsop_date")
        val stckBsopDate: String,
        @JsonProperty("stck_clpr")
        val stckClpr: String,
        @JsonProperty("stck_hgpr")
        val stckHgpr: String,
        @JsonProperty("stck_lwpr")
        val stckLwpr: String,
        @JsonProperty("stck_oprc")
        val stckOprc: String
    )
}