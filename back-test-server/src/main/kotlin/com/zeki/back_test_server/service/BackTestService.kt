package com.zeki.back_test_server.service

import com.zeki.algorithm.dto.AccountAsset
import com.zeki.back_test_server.config.MoleAlgorithmFactory
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.holiday.service.HolidayDateService
import com.zeki.mole_tunnel_db.entity.AlgorithmLog
import com.zeki.mole_tunnel_db.entity.AlgorithmLogDate
import com.zeki.mole_tunnel_db.entity.AlgorithmLogStock
import com.zeki.mole_tunnel_db.repository.AlgorithmLogRepository
import com.zeki.mole_tunnel_db.repository.AlgorithmRepository
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import com.zeki.mole_tunnel_db.repository.join.AlgorithmLogDateJoinRepository
import com.zeki.mole_tunnel_db.repository.join.AlgorithmLogStockJoinRepository
import org.slf4j.LoggerFactory
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
    private val algorithmLogRepository: AlgorithmLogRepository,
    private val algorithmLogDateRepository: AlgorithmLogDateJoinRepository,
    private val algorithmLogStockRepository: AlgorithmLogStockJoinRepository,
    private val holidayDateService: HolidayDateService,
    private val backTestTradeService: BackTestTradeService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun backTest(algorithmId: Long, startDate: LocalDate, endDate: LocalDate, deposit: BigDecimal) {
        logger.info(
            "백테스트 시작: 알고리즘 ID = ${algorithmId}, 기간 = ${startDate} ~ ${endDate}, 초기 자본금 = ${deposit}"
        )

        // 알고리즘 존재여부 파악, 백테스트니 알고리즘 상태와 상관없이 가져 옴
        val algorithmEntity =
            algorithmRepository.findById(algorithmId).orElseThrow {
                ApiException(ResponseCode.RESOURCE_NOT_FOUND)
            }

        // 팩토리를 통해 해당 알고리즘 Bean 획득
        val algorithm = moleAlgorithmFactory.getAlgorithmById(algorithmId)

        // 현 자산 정보 생성
        val backTestAsset = AccountAsset(depositPrice = deposit, valuationPrice = BigDecimal.ZERO)

        // 알고리즘 로그 생성
        val algorithmLog =
            AlgorithmLog.create(
                algorithm = algorithmEntity,
                startDate = startDate,
                endDate = endDate,
                depositPrice = backTestAsset.depositPrice,
            )

        // 알고리즘 로그를 먼저 저장하여 ID 할당
        val savedAlgorithmLog = algorithmLogRepository.save(algorithmLog)
        logger.info("알고리즘 로그 생성 및 저장 완료: ID = ${savedAlgorithmLog.id}")

        // 주식 코드 리스트 조회
        val findStockCode = stockCodeRepository.findAllByIsAlive().subList(450, 460)
        val stockCodeList = findStockCode.map { it.code }
        logger.info("분석 대상 종목 수: ${stockCodeList.size}")

        // 휴일을 제외한 날짜 리스트 생성
        val dateList = holidayDateService.getAvailableDateList(startDate, endDate)
        logger.info("백테스트 대상 날짜 수: ${dateList.size}")

        // 순회전 bulk insert용 List
        val algorithmLogDateSaveList = mutableListOf<AlgorithmLogDate>()
        val algorithmLogStockSaveList = mutableListOf<AlgorithmLogStock>()

        // 날짜 리스트를 순회하며 알고리즘 실행
        var processedDays = 0
        for ((i, today) in dateList.withIndex()) {
            // 다음날 시가가 없으므로 종료
            if (i == dateList.size - 1) break

            val nextDay = dateList[i + 1]
            logger.info("날짜 처리 중: ${today} -> ${nextDay}")

            // 해당일자의 stockPrice 조회
            val stockPriceMap =
                stockPriceRepository.findAllByStockInfo_CodeInAndDate(stockCodeList, nextDay)
                    .associateBy { it.stockInfo.code }

            if (stockPriceMap.isEmpty()) {
                logger.warn("${nextDay} 날짜에 대한 주가 데이터가 없습니다. 해당 날짜 처리를 건너뜁니다.")
                continue
            }

            // 알고리즘 실행
            val algoTradeList =
                algorithm.runAlgorithm(
                    stockCodeList = stockCodeList,
                    standradDate = today,
                    allowPrice = savedAlgorithmLog.depositPrice,
                    accountAsset = backTestAsset
                )

            logger.info("알고리즘 실행 결과: ${today} 날짜에 ${algoTradeList.size}개의 매매 신호 생성됨")

            // 매매 진행
            val (algorithmLogDate, algorithmLogStockList) =
                backTestTradeService.trade(
                    nextDay = nextDay,
                    backTestAsset = backTestAsset,
                    algorithmLog = savedAlgorithmLog,
                    algoTradeList = algoTradeList,
                    originDeposit = deposit,
                    stockPriceMap = stockPriceMap
                )

            // 평가금 재계산 - 모든 주식의 현재가 * 수량으로 평가금액 전체 재계산
            backTestAsset.valuationPrice = BigDecimal.ZERO // 평가금액 초기화

            // 보유중인 모든 주식에 대해 현재가 업데이트 및 평가금액 계산
            for ((stockCode, stockAsset) in backTestAsset.stockMap.entries) {
                stockAsset.holdingDays += 1
                stockAsset.currentStandardPrice =
                    stockPriceMap[stockCode]?.close ?: stockAsset.currentStandardPrice
                stockAsset.currentTotalPrice = stockAsset.currentStandardPrice * stockAsset.quantity

                // 평가금액에 각 종목 평가금액 합산
                backTestAsset.valuationPrice =
                    backTestAsset.valuationPrice.add(stockAsset.currentTotalPrice)
            }

            logger.info(
                "자산 평가 완료: 예수금=${backTestAsset.depositPrice}, 평가금액=${backTestAsset.valuationPrice}, 총액=${backTestAsset.depositPrice + backTestAsset.valuationPrice}"
            )

            algorithmLogDateSaveList.add(algorithmLogDate)
            algorithmLogStockSaveList.addAll(algorithmLogStockList)
            processedDays++
        }

        logger.info("백테스트 처리 완료: 총 ${processedDays}일 처리됨, 로그 데이터 저장 시작")
        logger.info("저장할 날짜별 로그 데이터: ${algorithmLogDateSaveList.size}개")
        logger.info("저장할 매매 로그 데이터: ${algorithmLogStockSaveList.size}개")

        // 알고리즘 로그 저장
        if (algorithmLogDateSaveList.isNotEmpty()) {
            algorithmLogDateRepository.bulkInsert(algorithmLogDateSaveList)
        }

        if (algorithmLogStockSaveList.isNotEmpty()) {
            algorithmLogStockRepository.bulkInsert(algorithmLogStockSaveList)
        }

        logger.info("백테스트 완료: 알고리즘 ID = ${algorithmId}, 로그 ID = ${savedAlgorithmLog.id}")
    }
}
