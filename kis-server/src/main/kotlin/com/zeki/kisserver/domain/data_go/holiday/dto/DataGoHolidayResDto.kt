package com.zeki.kisserver.domain.data_go.holiday.dto


data class DataGoHolidayResDto(
    val response: com.zeki.kisserver.domain.data_go.holiday.dto.Response = com.zeki.kisserver.domain.data_go.holiday.dto.Response()
)

data class Response(
    val body: com.zeki.kisserver.domain.data_go.holiday.dto.Body = com.zeki.kisserver.domain.data_go.holiday.dto.Body(),
    val header: com.zeki.kisserver.domain.data_go.holiday.dto.Header = com.zeki.kisserver.domain.data_go.holiday.dto.Header()
)

data class Body(
    val items: com.zeki.kisserver.domain.data_go.holiday.dto.Items = com.zeki.kisserver.domain.data_go.holiday.dto.Items(),
    val numOfRows: Int = 0,
    val pageNo: Int = 0,
    val totalCount: Int = 0
)

data class Header(
    val resultCode: String = "",
    val resultMsg: String = ""
)

data class Items(
    val item: List<com.zeki.kisserver.domain.data_go.holiday.dto.HolidayItem> = listOf()
)

data class HolidayItem(
    val dateKind: String = "",
    val dateName: String = "",
    val isHoliday: String = "",
    val locdate: Int = 0,
    val seq: Int = 0
)