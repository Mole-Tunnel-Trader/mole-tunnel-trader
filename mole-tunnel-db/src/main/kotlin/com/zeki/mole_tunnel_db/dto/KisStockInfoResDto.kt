package com.zeki.mole_tunnel_db.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KisStockInfoResDto(
    @JsonProperty("msg1")
    val msg1: String,
    @JsonProperty("msg_cd")
    val msgCd: String,
    @JsonProperty("output1")
    val output1: Output1?,
    @JsonProperty("output2")
    val output2: List<Output2> = emptyList(),
    @JsonProperty("rt_cd")
    val rtCd: String
) {
    data class Output1(
        @JsonProperty("acml_tr_pbmn")
        val acmlTrPbmn: String? = null,
        @JsonProperty("acml_vol")
        val acmlVol: String? = null,
        @JsonProperty("askp")
        val askp: String? = null,
        @JsonProperty("bidp")
        val bidp: String? = null,
        @JsonProperty("cpfn")
        val cpfn: String? = null,
        @JsonProperty("eps")
        val eps: String? = null,
        @JsonProperty("hts_avls")
        val htsAvls: String? = null,
        @JsonProperty("hts_kor_isnm")
        val stockName: String? = null,
        @JsonProperty("itewhol_loan_rmnd_ratem name")
        val itewholLoanRmndRatemName: String? = null,
        @JsonProperty("lstn_stcn")
        val lstnStcn: String? = null,
        @JsonProperty("pbr")
        val pbr: String? = null,
        @JsonProperty("per")
        val per: String? = null,
        @JsonProperty("prdy_ctrt")
        val prdyCtrt: String? = null,
        @JsonProperty("prdy_vol")
        val prdyVol: String? = null,
        @JsonProperty("prdy_vrss")
        val prdyVrss: String? = null,
        @JsonProperty("prdy_vrss_sign")
        val prdyVrssSign: String? = null,
        @JsonProperty("prdy_vrss_vol")
        val prdyVrssVol: String? = null,
        @JsonProperty("stck_fcam")
        val stckFcam: String? = null,
        @JsonProperty("stck_hgpr")
        val stckHgpr: String? = null,
        @JsonProperty("stck_llam")
        val stckLlam: String? = null,
        @JsonProperty("stck_lwpr")
        val stckLwpr: String? = null,
        @JsonProperty("stck_mxpr")
        val stckMxpr: String? = null,
        @JsonProperty("stck_oprc")
        val stckOprc: String? = null,
        @JsonProperty("stck_prdy_clpr")
        val stckPrdyClpr: String? = null,
        @JsonProperty("stck_prdy_hgpr")
        val stckPrdyHgpr: String? = null,
        @JsonProperty("stck_prdy_lwpr")
        val stckPrdyLwpr: String? = null,
        @JsonProperty("stck_prdy_oprc")
        val stckPrdyOprc: String? = null,
        @JsonProperty("stck_prpr")
        val stckPrpr: String? = null,
        @JsonProperty("stck_shrn_iscd")
        val stockCode: String? = null,
        @JsonProperty("vol_tnrt")
        val volTnrt: String? = null
    )

    data class Output2(
        @JsonProperty("acml_tr_pbmn")
        val acmlTrPbmn: String?,
        @JsonProperty("acml_vol")
        val acmlVol: String?,
        @JsonProperty("flng_cls_code")
        val flngClsCode: String?,
        @JsonProperty("mod_yn")
        val modYn: String?,
        @JsonProperty("prdy_vrss")
        val prdyVrss: String?,
        @JsonProperty("prdy_vrss_sign")
        val prdyVrssSign: String?,
        @JsonProperty("prtt_rate")
        val prttRate: String?,
        @JsonProperty("revl_issu_reas")
        val revlIssuReas: String?,
        @JsonProperty("stck_bsop_date")
        val stckBsopDate: String?,
        @JsonProperty("stck_clpr")
        val stckClpr: String?,
        @JsonProperty("stck_hgpr")
        val stckHgpr: String?,
        @JsonProperty("stck_lwpr")
        val stckLwpr: String?,
        @JsonProperty("stck_oprc")
        val stckOprc: String?
    )
}
