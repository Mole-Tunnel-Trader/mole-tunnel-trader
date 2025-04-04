package com.zeki.kisserver.domain.kis.trade

import com.zeki.common.em.TradeMode
import com.zeki.mole_tunnel_db.dto.TradeQueueDto
import com.zeki.mole_tunnel_db.repository.AccountAlgorithmRepository
import com.zeki.mole_tunnel_db.repository.AccountRepository
import com.zeki.mole_tunnel_db.repository.TradeQueueRepository
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TradeQueueService(
        private val tradeQueueRepository: TradeQueueRepository,
        private val accountAlgorithmRepository: AccountAlgorithmRepository,
        private val accountRepository: AccountRepository
) {
    /**
     * tradeQueue 테이블에서 데이터를 조회하고 계좌와 알고리즘 연동 정보(rate)를 고려하여 실제 매매를 위한 TradeQueueDto 목록을 생성합니다.
     *
     * 각 계좌별로 알고리즘 연동 비율을 고려하여 매매 수량을 계산합니다.
     */
    @Transactional(readOnly = true)
    fun getTradeQueue(): List<TradeQueueDto> {
        // 오늘 날짜에 해당하는 tradeQueue 데이터 조회
        val today = LocalDate.now()
        val tradeQueueList = tradeQueueRepository.findByOrderDate(today)

        if (tradeQueueList.isEmpty()) {
            return emptyList()
        }

        // 매매 실행 계좌 조회 (실제 환경에서 사용할 계좌)
        val liveAccounts =
                accountRepository.findByAccountTypeIn(listOf(TradeMode.REAL, TradeMode.TRAIN))

        // 알고리즘별로 그룹화
        val tradeQueueByAlgorithm = tradeQueueList.groupBy { it.algorithm }

        // 각 알고리즘별로 처리하여 TradeQueueDto 목록 생성
        return tradeQueueByAlgorithm.mapNotNull { (algorithm, tradeQueues) ->
            // 해당 알고리즘과 연결된 계좌 연동 정보 조회
            val accountAlgorithms = accountAlgorithmRepository.findByAlgorithm(algorithm)

            // 알고리즘과 연동된 계좌가 없으면 처리하지 않음
            if (accountAlgorithms.isEmpty()) {
                return@mapNotNull null
            }

            // 실제 사용할 계좌와 연결된 계좌 알고리즘만 필터링
            val validAccountAlgorithms =
                    accountAlgorithms.filter { liveAccounts.contains(it.account) }

            if (validAccountAlgorithms.isEmpty()) {
                return@mapNotNull null
            }

            // flatMap을 사용하여 이중 for문 대체
            val tradeQueueItems =
                    tradeQueues.flatMap { tradeQueue ->
                        validAccountAlgorithms.map { accountAlgorithm ->
                            // 계좌의 알고리즘 연동 비율에 따라 매매 수량 계산
                            // orderAmountRate는 알고리즘이 판단한 종목별 매매 비율
                            // tradePriceRate는 계좌의 알고리즘 연동 비율
                            val orderAmount =
                                    tradeQueue.orderAmountRate *
                                            accountAlgorithm.tradePriceRate.toBigDecimal()

                            // 매매 항목 생성
                            TradeQueueDto.Item(
                                    id = tradeQueue.id!!,
                                    stockCode = tradeQueue.stockCode,
                                    orderType = tradeQueue.orderType,
                                    orderPrice = tradeQueue.orderPrice,
                                    orderAmount = orderAmount.toDouble(),
                                    account = accountAlgorithm.account
                            )
                        }
                    }

            // 매매 항목이 없으면 null 반환하여 필터링
            if (tradeQueueItems.isEmpty()) {
                null
            } else {
                // 알고리즘 이름을 orderBy로 사용하여 TradeQueueDto 생성
                TradeQueueDto(orderBy = algorithm.name, items = tradeQueueItems)
            }
        }
    }

    /** 처리가 완료된 tradeQueue를 삭제합니다. */
    @Transactional
    fun removeTradeQueue(tradeQueueIdList: List<Long>) {
        if (tradeQueueIdList.isNotEmpty()) {
            tradeQueueRepository.deleteAllByIdInBatch(tradeQueueIdList)
        }
    }
}
