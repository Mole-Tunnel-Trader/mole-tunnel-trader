package com.zeki.cursor_algorithm.service

import com.zeki.mole_tunnel_db.entity.StockPrice
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import com.zeki.stockdata.service.stock_price.StockPriceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class CursorIndicatorService(
    private val stockPriceRepository: StockPriceRepository,
    private val stockCodeRepository: StockCodeRepository,
    private val stockPriceService: StockPriceService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 특정 날짜에 대한 모든 종목의 지표를 계산하고 저장
     * @param date 지표 계산 기준일
     * @return 생성/업데이트된 지표 수
     */
    @Transactional
    fun calculateAndSaveIndicators(date: LocalDate, stockCodeList: List<String>): Int {
        logger.info("볼륨 지표 계산 시작: $date")

        if (stockCodeList.isEmpty()) {
            logger.warn("활성 종목이 없습니다. 지표 계산을 건너뜁니다.")
            return 0
        }

        logger.info("총 ${stockCodeList.size}개 종목에 대한 지표 계산 시작")

        // StockPriceService의 updateVolumeIndicators 메소드를 호출하여 지표 업데이트
        val updatedCount = stockPriceService.updateVolumeIndicators(stockCodeList, date)

        logger.info("볼륨 지표 계산 완료: $date, 처리된 종목 수: $updatedCount")
        return updatedCount
    }

    /**
     * 특정 종목의 현재가 조회
     * @param stockCode 종목코드
     * @param date 기준일
     * @return 현재가 (해당 일자의 종가)
     */
    fun getLatestPrice(stockCode: String, date: LocalDate): BigDecimal? {
        val stockPrice = stockPriceRepository.findByStockInfo_CodeAndDate(stockCode, date)
        return stockPrice?.close
    }

    /**
     * 특정 날짜의 거래량 급증 종목 조회
     * @param date 기준일
     * @param minVolumeRatio 최소 거래량 비율 (기본값: 2.0 - 평균 대비 2배)
     * @param priceIncreaseOnly 가격 상승 종목만 조회할지 여부
     * @return 거래량 급증 종목 리스트
     */
    fun getVolumeSpikes(
        date: LocalDate,
        minVolumeRatio: BigDecimal = BigDecimal("2.0"),
        priceIncreaseOnly: Boolean = false
    ): List<StockPrice> {
        // 이제 StockPriceService의 메소드를 직접 활용
        return stockPriceService.getVolumeSpikes(date, minVolumeRatio, priceIncreaseOnly)
    }

    /**
     * 특정 날짜의 거래량이 급증했지만 가격이 하락한 종목 조회 (기회 포착용)
     * @param date 기준일
     * @param minVolumeRatio 최소 거래량 비율 (기본값: 2.0 - 평균 대비 2배)
     * @return 거래량 급증 & 가격 하락 종목 리스트
     */
    fun getVolumeSpikeWithPriceDecrease(
        date: LocalDate,
        minVolumeRatio: BigDecimal = BigDecimal("2.0")
    ): List<StockPrice> {
        val stockPrices = stockPriceRepository.findAllByDateOrderByStockInfoCode(date)

        return stockPrices
            .filter { stockPrice ->
                // 거래량 비율이 기준치 이상이고 가격 변동률이 음수인 종목
                stockPrice.volumeRatio != null &&
                        stockPrice.volumeRatio!! >= minVolumeRatio &&
                        stockPrice.priceChangeRate != null &&
                        stockPrice.priceChangeRate!! < BigDecimal.ZERO
            }
            .sortedByDescending { it.volumeRatio }
    }

    /**
     * 특정 날짜의 변동성이 큰 종목 조회
     * @param date 기준일
     * @param minVolumeRatio 최소 거래량 비율
     * @param minVolatility 최소 변동성
     * @return 변동성이 큰 종목 리스트
     */
    fun getHighVolatilityStocks(
        date: LocalDate,
        minVolumeRatio: BigDecimal = BigDecimal("1.5"),
        minVolatility: BigDecimal = BigDecimal("0.05") // 5% 이상 변동성
    ): List<StockPrice> {
        // 이제 StockPriceService의 메소드를 직접 활용
        return stockPriceService.getHighVolatilityStocks(date, minVolumeRatio, minVolatility)
    }
}
