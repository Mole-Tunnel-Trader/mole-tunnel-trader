package com.zeki.back_test_server.config

import com.zeki.algorithm.MoleAlgorithm
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import org.springframework.stereotype.Service

@Service
class MoleAlgorithmFactory(
    private val algorithms: List<MoleAlgorithm>
) {
    fun getAlgorithmById(algorithmId: Long): MoleAlgorithm {
        // ID가 일치하는 알고리즘 Bean을 반환
        return algorithms.find { it.id == algorithmId }
            ?: throw ApiException(
                ResponseCode.RESOURCE_NOT_FOUND,
                "해당 알고리즘(ID=$algorithmId)이 존재하지 않습니다."
            )
    }
}
