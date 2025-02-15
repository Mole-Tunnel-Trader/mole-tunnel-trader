package com.zeki.mole_tunnel_db.dto

data class DataGoStockCodeResDto(
    val response: Response = Response()
) {

    data class Response(
        val body: Body = Body(),
        val header: Header = Header()
    )

    data class Body(
        val items: Items = Items(),
        val numOfRows: Int = 0,
        val pageNo: Int = 0,
        val totalCount: Int = 0
    )

    data class Header(
        val resultCode: String = "",
        val resultMsg: String = ""
    )

    data class Items(
        val item: List<StockCodeItem> = listOf()
    )

    data class StockCodeItem(
        val basDt: String = "",
        val corpNm: String = "",
        val crno: String = "",
        val isinCd: String = "",
        val itmsNm: String = "",
        val mrktCtg: String = "",
        val srtnCd: String = ""
    )
}
