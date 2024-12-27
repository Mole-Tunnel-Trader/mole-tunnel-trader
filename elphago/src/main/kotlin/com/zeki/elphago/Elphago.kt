package com.zeki.elphago

import com.zeki.common.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "elphago")
class Elphago(
    code: String,
    name: String? = null,
    status: ElphagoStatus = ElphagoStatus.NOTTING,
    startDate: LocalDate,
    nowDate: LocalDate,
    // 알고리즘 상태를 저장하기 위한 변수들 추가
    var lineFlag: Int = -1,
    var lineCnt: Int = 0,
    var signalFlag: Int = 0,
    var buyPrice: BigDecimal = BigDecimal.ONE,
    var buyDate: LocalDate? = null,
    var buyDateCnt: Int = 0,
    var volStack: Int = 0,
    // temp 변수들 추가 (BigDecimal로 변경)
    var temp1Low: BigDecimal = BigDecimal.ZERO,
    var temp1High: BigDecimal = BigDecimal.ZERO,
    var temp1Close: BigDecimal = BigDecimal.ZERO,
    var temp2Low: BigDecimal = BigDecimal.ZERO,
    var temp2High: BigDecimal = BigDecimal.ZERO,
    var temp2Close: BigDecimal = BigDecimal.ZERO,
    var temp3Low: BigDecimal = BigDecimal.ZERO,
    var temp3High: BigDecimal = BigDecimal.ZERO,
    var temp3Close: BigDecimal = BigDecimal.ZERO,
    var temp4Low: BigDecimal = BigDecimal.ZERO,
    var temp4High: BigDecimal = BigDecimal.ZERO,
    var temp4Close: BigDecimal = BigDecimal.ZERO,
    var temp5Low: BigDecimal = BigDecimal.ZERO,
    var temp5High: BigDecimal = BigDecimal.ZERO,
    var temp5Close: BigDecimal = BigDecimal.ZERO,
    var tempaLow: BigDecimal = BigDecimal.ZERO,
    var tempaHigh: BigDecimal = BigDecimal.ZERO,
    var tempaClose: BigDecimal = BigDecimal.ZERO
) : BaseEntity() {

    var code: String = code
        protected set

    var name: String? = name
        protected set


    var startDate: LocalDate = startDate

    var nowDate: LocalDate = nowDate
}
