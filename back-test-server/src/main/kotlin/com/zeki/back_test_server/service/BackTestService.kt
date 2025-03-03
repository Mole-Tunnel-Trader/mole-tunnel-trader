package com.zeki.back_test_server.service

import com.zeki.back_test_server.config.MoleAlgorithmFactory
import com.zeki.back_test_server.dto.BackTestAsset
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.holiday.service.HolidayDateService
import com.zeki.mole_tunnel_db.entity.AlgorithmLog
import com.zeki.mole_tunnel_db.repository.AlgorithmLogDateDetailRepository
import com.zeki.mole_tunnel_db.repository.AlgorithmLogRepository
import com.zeki.mole_tunnel_db.repository.AlgorithmRepository
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BackTestService(
        private val moleAlgorithmFactory: MoleAlgorithmFactory,

        private val stockCodeRepository: StockCodeRepository,
        private val algorithmRepository: AlgorithmRepository,
        private val algorithmLogRepository: AlgorithmLogRepository,
        private val algorithmLogDateDetailRepository: AlgorithmLogDateDetailRepository,

        private val holidayDateService: HolidayDateService,
        private val backTestTradeService: BackTestTradeService
) {

    @Transactional
    fun backTest(algorithmId: Long, startDate: LocalDate, endDate: LocalDate, deposit: BigDecimal) {
        // 알고리즘 존재여부 파악, 백테스트니 알고리즘 상태와 상관없이 가져 옴
        val algorithmEntity = algorithmRepository.findById(algorithmId)
                .orElseThrow { ApiException(ResponseCode.RESOURCE_NOT_FOUND) }

        // 팩토리를 통해 해당 알고리즘 Bean 획득
        val algorithm = moleAlgorithmFactory.getAlgorithmById(algorithmId)

        // 현 자산 정보 생성
        val backTestAsset = BackTestAsset(
                depositPrice = deposit,
                valuationPrice = BigDecimal.ZERO
        )

        // 알고리즘 로그 생성
        val algorithmLog = AlgorithmLog.create(
                algorithm = algorithmEntity,
                startDate = startDate,
                endDate = endDate,
                depositPrice = backTestAsset.depositPrice,
                valuationPrice = backTestAsset.valuationPrice
        )

        // 주식 코드 리스트 조회
        val findStockCode = stockCodeRepository.findAllByIsAlive()
        val stockCodeList = findStockCode.map { it.code }

        // 휴일을 제외한 날짜 리스트 생성
        val dateList = holidayDateService.getAvailableDateList(startDate, endDate)

        // 날짜 리스트를 순회하며 알고리즘 실행
        for ((i, today) in dateList.withIndex()) {
            // 다음날 시작가 가 없으므로 종료
            if (i == dateList.size - 1) break

            val nextDay = dateList[i + 1]

            // 알고리즘 실행
            val algoTradeList =
                    algorithm.runAlgorithm(stockCodeList, today, algorithmLog.depositPrice)

            // 매매 진행
            val algorithmLogDateDetailList =
                    backTestTradeService.trade(
                            nextDay,
                            stockCodeList,
                            backTestAsset,
                            algorithmLog,
                            algoTradeList
                    )

            // 알고리즘 로그에 날짜별 상세 내역 저장
            // TODO : bulk insert 구현해야함
            algorithmLogDateDetailRepository.saveAll(algorithmLogDateDetailList)
        }

    }

}
