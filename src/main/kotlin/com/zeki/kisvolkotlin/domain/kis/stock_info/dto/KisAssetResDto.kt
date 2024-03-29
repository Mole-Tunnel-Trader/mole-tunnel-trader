package com.zeki.kisvolkotlin.domain.kis.stock_info.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class KisAssetResDto(
    @JsonProperty("ctx_area_fk100")
    val ctxAreaFk100: String,
    @JsonProperty("ctx_area_nk100")
    val ctxAreaNk100: String,
    @JsonProperty("msg1")
    val msg1: String,
    @JsonProperty("msg_cd")
    val msgCd: String,
    @JsonProperty("output1")
    val output1: List<Output1>,
    @JsonProperty("output2")
    val output2: List<Output2>,
    @JsonProperty("rt_cd")
    val rtCd: String
) {
    data class Output1(
        @JsonProperty("bfdy_buy_qty")
        val bfdyBuyQty: String,
        @JsonProperty("bfdy_cprs_icdc")
        val bfdyCprsIcdc: String,
        @JsonProperty("bfdy_sll_qty")
        val bfdySllQty: String,
        @JsonProperty("evlu_amt")
        val evluAmt: String,
        @JsonProperty("evlu_erng_rt")
        val evluErngRt: String,
        @JsonProperty("evlu_pfls_amt")
        val evluPflsAmt: String,
        @JsonProperty("evlu_pfls_rt")
        val evluPflsRt: String,
        @JsonProperty("expd_dt")
        val expdDt: String,
        @JsonProperty("fltt_rt")
        val flttRt: String,
        @JsonProperty("grta_rt_name")
        val grtaRtName: String,
        @JsonProperty("hldg_qty")
        val hldgQty: String,
        @JsonProperty("item_mgna_rt_name")
        val itemMgnaRtName: String,
        @JsonProperty("loan_amt")
        val loanAmt: String,
        @JsonProperty("loan_dt")
        val loanDt: String,
        @JsonProperty("ord_psbl_qty")
        val ordPsblQty: String,
        @JsonProperty("pchs_amt")
        val pchsAmt: String,
        @JsonProperty("pchs_avg_pric")
        val pchsAvgPric: String,
        @JsonProperty("pdno")
        val pdno: String,
        @JsonProperty("prdt_name")
        val prdtName: String,
        @JsonProperty("prpr")
        val prpr: String,
        @JsonProperty("sbst_pric")
        val sbstPric: String,
        @JsonProperty("stck_loan_unpr")
        val stckLoanUnpr: String,
        @JsonProperty("stln_slng_chgs")
        val stlnSlngChgs: String,
        @JsonProperty("thdt_buyqty")
        val thdtBuyqty: String,
        @JsonProperty("thdt_sll_qty")
        val thdtSllQty: String,
        @JsonProperty("trad_dvsn_name")
        val tradDvsnName: String
    )

    data class Output2(
        @JsonProperty("asst_icdc_amt")
        val asstIcdcAmt: String,
        @JsonProperty("asst_icdc_erng_rt")
        val asstIcdcErngRt: String,
        @JsonProperty("bfdy_buy_amt")
        val bfdyBuyAmt: String,
        @JsonProperty("bfdy_sll_amt")
        val bfdySllAmt: String,
        @JsonProperty("bfdy_tlex_amt")
        val bfdyTlexAmt: String,
        @JsonProperty("bfdy_tot_asst_evlu_amt")
        val bfdyTotAsstEvluAmt: String,
        @JsonProperty("cma_evlu_amt")
        val cmaEvluAmt: String,
        @JsonProperty("d2_auto_rdpt_amt")
        val d2AutoRdptAmt: String,
        @JsonProperty("dnca_tot_amt")
        val dncaTotAmt: String,
        @JsonProperty("evlu_amt_smtl_amt")
        val evluAmtSmtlAmt: String,
        @JsonProperty("evlu_pfls_smtl_amt")
        val evluPflsSmtlAmt: String,
        @JsonProperty("fncg_gld_auto_rdpt_yn")
        val fncgGldAutoRdptYn: String,
        @JsonProperty("nass_amt")
        val nassAmt: String,
        @JsonProperty("nxdy_auto_rdpt_amt")
        val nxdyAutoRdptAmt: String,
        @JsonProperty("nxdy_excc_amt")
        val nxdyExccAmt: String,
        @JsonProperty("pchs_amt_smtl_amt")
        val pchsAmtSmtlAmt: String,
        @JsonProperty("prvs_rcdl_excc_amt")
        val prvsRcdlExccAmt: String,
        @JsonProperty("scts_evlu_amt")
        val sctsEvluAmt: String,
        @JsonProperty("thdt_buy_amt")
        val thdtBuyAmt: String,
        @JsonProperty("thdt_sll_amt")
        val thdtSllAmt: String,
        @JsonProperty("thdt_tlex_amt")
        val thdtTlexAmt: String,
        @JsonProperty("tot_evlu_amt")
        val totEvluAmt: String,
        @JsonProperty("tot_loan_amt")
        val totLoanAmt: String,
        @JsonProperty("tot_stln_slng_chgs")
        val totStlnSlngChgs: String
    )
}