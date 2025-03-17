package com.zeki.kisserver.domain.kis.stock_info

import com.zeki.holiday.dto.report.UpsertReportDto
import com.zeki.mole_tunnel_db.entity.StockInfo
import com.zeki.mole_tunnel_db.repository.StockInfoRepository
import com.zeki.mole_tunnel_db.repository.join.StockInfoJoinRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StockInfoService(
    private val stockInfoRepository: StockInfoRepository,
    private val stockInfoJoinRepository: StockInfoJoinRepository,
    private val stockInfoConnectService: StockInfoConnectService
) {

    fun upsertStockInfo(stockCodeList: List<String>): UpsertReportDto {
        val stockInfoSaveList = mutableListOf<StockInfo>()
        val stockInfoUpdateList = mutableListOf<StockInfo>()

        val savedStockInfoMap =
            stockInfoRepository
                .findByCodeIn(stockCodeList)
                .associateBy { it.code }
                .toMutableMap()

        val kisStockInfoDtoList = stockInfoConnectService.getKisStockInfoDtoList(stockCodeList)

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

        stockInfoJoinRepository.bulkInsert(stockInfoSaveList)
        stockInfoJoinRepository.bulkUpdate(stockInfoUpdateList)

        return UpsertReportDto(stockInfoSaveList.size, stockInfoUpdateList.size, 0)
    }

    @Transactional
    fun getStockInfoList(stockCodeList: List<String>): List<StockInfo> {
        return stockInfoRepository.findByCodeInWithStockPrices(stockCodeList)
    }
}
