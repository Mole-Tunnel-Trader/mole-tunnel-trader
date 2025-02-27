package com.zeki.back_test_server.service

import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.mole_tunnel_db.repository.AlgorithmLogRepository
import com.zeki.mole_tunnel_db.repository.AlgorithmRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BackTestService(
    private val algorithmRepository: AlgorithmRepository,
    private val algorithmLogRepository: AlgorithmLogRepository,
) {

    /**
     * 백테스트를 실행합니다.
     * 긴 실행시간이 예상되므로 Transactional을 사용하지 않습니다.
     */
    fun backTest(algorithmId: Long, startDate: LocalDate, endDate: LocalDate, deposit: BigDecimal) {
        // 알고리즘 존재여부 파악, 백테스트니 알고리즘 상태와 상관없이 가져 옴
        val algorithm = algorithmRepository.findById(algorithmId)
            .orElseThrow { ApiException(ResponseCode.RESOURCE_NOT_FOUND) }

        // 백테스트 실행
    }

}
