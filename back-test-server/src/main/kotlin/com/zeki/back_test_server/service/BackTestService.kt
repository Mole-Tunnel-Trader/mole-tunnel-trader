package com.zeki.back_test_server.service

import com.zeki.algorithm.config.MoleAlgorithmFactory
import com.zeki.back_test_server.dto.BackTestAsset
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.holiday.service.HolidayDateService
import com.zeki.mole_tunnel_db.entity.AlgorithmLog
import com.zeki.mole_tunnel_db.entity.AlgorithmLogDate
import com.zeki.mole_tunnel_db.entity.AlgorithmLogStock
import com.zeki.mole_tunnel_db.repository.AlgorithmRepository
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import com.zeki.mole_tunnel_db.repository.join.AlgorithmLogDateJoinRepository
import com.zeki.mole_tunnel_db.repository.join.AlgorithmLogStockJoinRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BackTestService(
    private val moleAlgorithmFactory: MoleAlgorithmFactory,

    private val stockCodeRepository: StockCodeRepository,
    private val stockPriceRepository: StockPriceRepository,
    private val algorithmRepository: AlgorithmRepository,

    private val algorithmLogDateRepository: AlgorithmLogDateJoinRepository,
    private val algorithmLogStockRepository: AlgorithmLogStockJoinRepository,

    private val holidayDateService: HolidayDateService,
    private val backTestTradeService: BackTestTradeService,

    private val entityManager: EntityManager,
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
        )

        entityManager.persist(algorithmLog)

        // 주식 코드 리스트 조회
        val findStockCode = stockCodeRepository.findAllByIsAlive()
        val stockCodeList = findStockCode.map { it.code }

        // 휴일을 제외한 날짜 리스트 생성
        val dateList = holidayDateService.getAvailableDateList(startDate, endDate)


        // 순회전 bulk insert용 List
        val algorithmLogDateSaveList = mutableListOf<AlgorithmLogDate>()
        val algorithmLogStockSaveList = mutableListOf<AlgorithmLogStock>()
        // 날짜 리스트를 순회하며 알고리즘 실행
        for ((i, today) in dateList.withIndex()) {
            // 다음날 시가가 없으므로 종료
            if (i == dateList.size - 1) break

            val nextDay = dateList[i + 1]

            // 해당일자의 stockPrice 조회
            val stockPriceMap =
                stockPriceRepository.findAllByStockInfo_CodeInAndDate(stockCodeList, nextDay)
                    .associateBy { it.stockInfo.code }


            // 알고리즘 실행
            val algoTradeList =
                algorithm.runAlgorithm(stockCodeList, today, algorithmLog.depositPrice)

            // 매매 진행
            val (algorithmLogDate, algorithmLogStockList) = backTestTradeService.trade(
                nextDay,
                stockCodeList,
                backTestAsset,
                algorithmLog,
                algoTradeList,
                deposit,
                stockPriceMap
            )

            // 평가금 계산
            for ((stockCode, stockAsset) in backTestAsset.stockMap.entries) {
                stockAsset.holdingDays += 1
                stockAsset.currentStandardPrice =
                    stockPriceMap[stockCode]?.close ?: stockAsset.currentStandardPrice
                val preTotalPrice = stockAsset.currentTotalPrice
                stockAsset.currentTotalPrice = stockAsset.currentStandardPrice * stockAsset.quantity
                backTestAsset.valuationPrice += stockAsset.currentTotalPrice - preTotalPrice
            }

            algorithmLogDateSaveList.add(algorithmLogDate)
            algorithmLogStockSaveList.addAll(algorithmLogStockList)
        }

        // 알고리즘 로그 저장
        algorithmLogDateRepository.bulkInsert(algorithmLogDateSaveList)
        algorithmLogStockRepository.bulkInsert(algorithmLogStockSaveList)
    }

}
