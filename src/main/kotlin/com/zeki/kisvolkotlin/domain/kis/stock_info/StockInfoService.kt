package com.zeki.kisvolkotlin.domain.kis.stock_info

import com.zeki.kisvolkotlin.db.entity.StockInfo
import com.zeki.kisvolkotlin.db.repository.StockInfoJoinRepository
import com.zeki.kisvolkotlin.db.repository.StockInfoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StockInfoService(
    private val stockInfoRepository: StockInfoRepository,
    private val stockInfoJoinRepository: StockInfoJoinRepository,

    private val stockInfoWebClientService: StockInfoWebClientService
) {

    @Transactional
    fun upsertStockInfo(stockCodeList: List<String>) {
        val stockInfoSaveList = mutableListOf<StockInfo>()
        val stockInfoUpdateList = mutableListOf<StockInfo>()

        val savedStockInfoMap = stockInfoRepository.findByCodeIn(stockCodeList)
            .associateBy { it.code }
            .toMutableMap()

        val kisStockInfoDtoList = stockInfoWebClientService.getKisStockInfoDtoList(stockCodeList)

        kisStockInfoDtoList.forEach { kisStockInfoDto ->
            val output1 = kisStockInfoDto.output1
            when (val stockInfo = savedStockInfoMap[output1.stockCode]) {
                null -> {
                    stockInfoSaveList.add(
                        StockInfo(
                            code = output1.stockCode,
                            name = output1.stockName,
                            otherCode = output1.lstnStcn,
                            fcamt = output1.stckFcam.toInt(),
                            amount = output1.lstnStcn.toLong(),
                            marketCapital = output1.htsAvls.toLong(),
                            capital = output1.cpfn.toLong(),
                            per = output1.per.toDouble(),
                            pbr = output1.pbr.toDouble(),
                            eps = output1.eps.toDouble()
                        )
                    )
                }

                else -> {
                    val isUpdate =
                        stockInfo.updateStockInfo(
                            name = output1.stockName,
                            otherCode = output1.lstnStcn,
                            fcamt = output1.stckFcam.toInt(),
                            amount = output1.lstnStcn.toLong(),
                            marketCapital = output1.htsAvls.toLong(),
                            capital = output1.cpfn.toLong(),
                            per = output1.per.toDouble(),
                            pbr = output1.pbr.toDouble(),
                            eps = output1.eps.toDouble()
                        )

                    if (isUpdate) stockInfoUpdateList.add(stockInfo)
                }
            }
        }

        stockInfoJoinRepository.bulkInsert(stockInfoSaveList)
        stockInfoJoinRepository.bulkUpdate(stockInfoUpdateList)
    }

    fun getStockInfoList(stockCodeList: List<String>): List<StockInfo> {
        return stockInfoRepository.findByCodeIn(stockCodeList)
    }
}