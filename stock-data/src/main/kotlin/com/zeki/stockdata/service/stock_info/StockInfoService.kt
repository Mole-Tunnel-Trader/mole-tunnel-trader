package com.zeki.stockdata.service.stock_info

import com.zeki.common.dto.UpsertReportDto
import com.zeki.mole_tunnel_db.entity.StockInfo
import com.zeki.mole_tunnel_db.repository.StockInfoRepository
import com.zeki.mole_tunnel_db.repository.join.StockInfoJoinRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StockInfoService(
    private val stockInfoRepository: StockInfoRepository,
    private val stockInfoJoinRepository: StockInfoJoinRepository,
    private val stockInfoConnectService: StockInfoConnectService
) {
    private val log = KotlinLogging.logger {}

    // 배치 처리 사이즈 설정
    private val batchSize = 500

    fun upsertStockInfo(stockCodeList: List<String>): UpsertReportDto {
        // 결과를 합산하기 위한 변수들
        val totalStockInfoSaveList = mutableListOf<StockInfo>()
        val totalStockInfoUpdateList = mutableListOf<StockInfo>()

        // 500개씩 나누어 처리
        stockCodeList.chunked(batchSize).forEach { batchCodes ->
            val stockInfoSaveList = mutableListOf<StockInfo>()
            val stockInfoUpdateList = mutableListOf<StockInfo>()

            val savedStockInfoMap =
                stockInfoRepository
                    .findByCodeIn(batchCodes)
                    .associateBy { it.code }
                    .toMutableMap()

            val kisStockInfoDtoList = stockInfoConnectService.getKisStockInfoDtoList(batchCodes)

            kisStockInfoDtoList.forEach { kisStockInfoDto ->
                val output1 = kisStockInfoDto.output1 ?: return@forEach

                when (val stockInfo = savedStockInfoMap[output1.stockCode]) {
                    null -> {
                        stockInfoSaveList.add(
                            StockInfo(
                                code = output1.stockCode ?: "",
                                name = output1.stockName ?: "",
                                otherCode = output1.lstnStcn ?: "",
                                fcamt = (output1.stckFcam?.toInt() ?: 0),
                                amount = (output1.lstnStcn?.toLongOrNull() ?: 0L),
                                marketCapital = (output1.htsAvls?.toLongOrNull() ?: 0L),
                                capital = (output1.cpfn?.toLongOrNull() ?: 0L),
                                per = (output1.per?.toDoubleOrNull() ?: 0.0),
                                pbr = (output1.pbr?.toDoubleOrNull() ?: 0.0),
                                eps = (output1.eps?.toDoubleOrNull() ?: 0.0)
                            )
                        )
                    }

                    else -> {
                        val isUpdate =
                            stockInfo.updateStockInfo(
                                name = output1.stockName ?: "",
                                otherCode = output1.lstnStcn ?: "",
                                fcamt = (output1.stckFcam?.toInt() ?: 0),
                                amount = (output1.lstnStcn?.toLongOrNull() ?: 0L),
                                marketCapital = (output1.htsAvls?.toLongOrNull() ?: 0L),
                                capital = (output1.cpfn?.toLongOrNull() ?: 0L),
                                per = (output1.per?.toDoubleOrNull() ?: 0.0),
                                pbr = (output1.pbr?.toDoubleOrNull() ?: 0.0),
                                eps = (output1.eps?.toDoubleOrNull() ?: 0.0)
                            )

                        if (isUpdate) stockInfoUpdateList.add(stockInfo)
                    }
                }
            }

            // 결과 리스트에 추가
            totalStockInfoSaveList.addAll(stockInfoSaveList)
            totalStockInfoUpdateList.addAll(stockInfoUpdateList)
        }

        // 모든 배치의 결과를 모아서 한 번에 벌크 작업 실행
        stockInfoJoinRepository.bulkInsert(totalStockInfoSaveList)
        stockInfoJoinRepository.bulkUpdate(totalStockInfoUpdateList)

        return UpsertReportDto(totalStockInfoSaveList.size, totalStockInfoUpdateList.size, 0)
    }

    @Transactional(readOnly = true)
    fun getStockInfoList(stockCodeList: List<String>): List<StockInfo> {
        return stockInfoRepository.findByCodeIn(stockCodeList)
    }
}
