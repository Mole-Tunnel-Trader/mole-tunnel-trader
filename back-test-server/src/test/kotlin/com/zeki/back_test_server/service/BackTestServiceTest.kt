package com.zeki.back_test_server.service

import com.zeki.algorithm.config.MoleAlgorithmFactory
import com.zeki.common.em.Status
import com.zeki.common.em.StockMarket
import com.zeki.common.exception.ApiException
import com.zeki.common.exception.ResponseCode
import com.zeki.holiday.service.HolidayDateService
import com.zeki.mole_tunnel_db.entity.Algorithm
import com.zeki.mole_tunnel_db.entity.StockCode
import com.zeki.mole_tunnel_db.entity.StockInfo
import com.zeki.mole_tunnel_db.entity.StockPrice
import com.zeki.mole_tunnel_db.repository.AlgorithmRepository
import com.zeki.mole_tunnel_db.repository.StockCodeRepository
import com.zeki.mole_tunnel_db.repository.StockInfoRepository
import com.zeki.mole_tunnel_db.repository.StockPriceRepository
import com.zeki.mole_tunnel_db.repository.join.AlgorithmLogDateJoinRepository
import com.zeki.mole_tunnel_db.repository.join.AlgorithmLogStockJoinRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mu.KotlinLogging
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class BackTestServiceTest(
    private val backTestService: BackTestService,
    private val moleAlgorithmFactory: MoleAlgorithmFactory,
    private val algorithmRepository: AlgorithmRepository,
    private val stockCodeRepository: StockCodeRepository,
    private val stockInfoRepository: StockInfoRepository,
    private val stockPriceRepository: StockPriceRepository,
    private val algorithmLogDateRepository: AlgorithmLogDateJoinRepository,
    private val algorithmLogStockRepository: AlgorithmLogStockJoinRepository,
    private val holidayDateService: HolidayDateService,
    private val transactionManager: PlatformTransactionManager
) :
    BehaviorSpec({
        val log = KotlinLogging.logger {}
        val transactionTemplate = TransactionTemplate(transactionManager)

        // 테스트에 사용할 변수들
        lateinit var testAlgorithm: Algorithm
        lateinit var samsungStockInfo: StockInfo
        lateinit var kakaoStockInfo: StockInfo
        lateinit var naverStockInfo: StockInfo
        val stockPrices = mutableListOf<StockPrice>()
        val stockCodes = mutableListOf<StockCode>()

        beforeSpec {
            // 테스트 데이터 설정
            transactionTemplate.execute {
                // 1. 알고리즘 데이터 생성
                testAlgorithm = Algorithm.create(Status.Y, "테스트 알고리즘 1")
                algorithmRepository.save(testAlgorithm)

                // 2. 주식 코드 데이터 생성
                val samsungStockCode = StockCode("005930", "삼성전자", StockMarket.KOSPI, Status.Y)
                val kakaoStockCode = StockCode("035720", "카카오", StockMarket.KOSPI, Status.Y)
                val naverStockCode = StockCode("035420", "NAVER", StockMarket.KOSPI, Status.Y)

                stockCodes.addAll(listOf(samsungStockCode, kakaoStockCode, naverStockCode))
                stockCodeRepository.saveAll(stockCodes)

                // 3. 주식 정보 데이터 생성
                samsungStockInfo =
                    StockInfo(
                        name = "삼성전자",
                        code = "005930",
                        otherCode = "005930",
                        fcamt = 100,
                        amount = 1000000L,
                        marketCapital = 10000000000L,
                        capital = 5000000000L,
                        per = 10.0,
                        pbr = 1.5,
                        eps = 5000.0
                    )

                kakaoStockInfo =
                    StockInfo(
                        name = "카카오",
                        code = "035720",
                        otherCode = "035720",
                        fcamt = 500,
                        amount = 500000L,
                        marketCapital = 5000000000L,
                        capital = 2500000000L,
                        per = 20.0,
                        pbr = 2.5,
                        eps = 2500.0
                    )

                naverStockInfo =
                    StockInfo(
                        name = "NAVER",
                        code = "035420",
                        otherCode = "035420",
                        fcamt = 500,
                        amount = 300000L,
                        marketCapital = 9000000000L,
                        capital = 3000000000L,
                        per = 25.0,
                        pbr = 3.0,
                        eps = 12000.0
                    )

                stockInfoRepository.saveAll(
                    listOf(samsungStockInfo, kakaoStockInfo, naverStockInfo)
                )

                // 4. 주식 가격 데이터 생성
                val testDates =
                    listOf(
                        LocalDate.of(2023, 1, 2),
                        LocalDate.of(2023, 1, 3),
                        LocalDate.of(2023, 1, 4)
                    )

                // 삼성전자 주가 데이터
                testDates.forEach { date ->
                    val stockPrice =
                        StockPrice.create(
                            stockInfo = samsungStockInfo,
                            date = date,
                            open = BigDecimal(60000),
                            high = BigDecimal(62000),
                            low = BigDecimal(59000),
                            close = BigDecimal(61000),
                            volume = 10000000L
                        )
                    stockPrices.add(stockPrice)
                }

                // 카카오 주가 데이터
                testDates.forEach { date ->
                    val stockPrice =
                        StockPrice.create(
                            stockInfo = kakaoStockInfo,
                            date = date,
                            open = BigDecimal(50000),
                            high = BigDecimal(52000),
                            low = BigDecimal(49000),
                            close = BigDecimal(51000),
                            volume = 5000000L
                        )
                    stockPrices.add(stockPrice)
                }

                // 네이버 주가 데이터
                testDates.forEach { date ->
                    val stockPrice =
                        StockPrice.create(
                            stockInfo = naverStockInfo,
                            date = date,
                            open = BigDecimal(300000),
                            high = BigDecimal(310000),
                            low = BigDecimal(295000),
                            close = BigDecimal(305000),
                            volume = 2000000L
                        )
                    stockPrices.add(stockPrice)
                }

                stockPriceRepository.saveAll(stockPrices)
                null
            }
        }

        afterSpec {
            // 테스트 후 정리 작업
            transactionTemplate.execute {
                try {
                    // 주식 가격 데이터 제거
                    stockPriceRepository.deleteAll(stockPrices)

                    // 주식 정보 데이터 제거
                    stockInfoRepository.deleteAll(
                        listOf(samsungStockInfo, kakaoStockInfo, naverStockInfo)
                    )

                    // 주식 코드 데이터 제거
                    stockCodeRepository.deleteAll(stockCodes)

                    // 알고리즘 데이터 제거
                    algorithmRepository.delete(testAlgorithm)
                } catch (e: Exception) {
                    println("테스트 데이터 정리 중 오류 발생: ${e.message}")
                }
                null
            }
        }

        Given("알고리즘 호출시") {
            When("해당 ID의 알고리즘이 존재시") {
                Then("알고리즘이 정상적으로 호출된다") {
                    transactionTemplate.execute {
                        val algorithm =
                            moleAlgorithmFactory.getAlgorithmById(testAlgorithm.id!!)
                        algorithm shouldNotBe null
                        algorithm.name shouldBe "테스트 알고리즘 1"
                        null
                    }
                }
            }
            When("해당 ID의 알고리즘이 존재하지 않을시") {
                val algorithmId = 999L
                Then("알고리즘이 호출되지 않는다") {
                    val exception =
                        shouldThrow<ApiException> {
                            moleAlgorithmFactory.getAlgorithmById(algorithmId)
                        }
                    exception.responseCode shouldBe ResponseCode.RESOURCE_NOT_FOUND
                }
            }
        }

        Given("백테스트 실행시") {
            val startDate = LocalDate.of(2023, 1, 1)
            val endDate = LocalDate.of(2023, 1, 5)
            val deposit = BigDecimal(10000000)

//                When("유효한 알고리즘 ID로 백테스트 실행시") {
//                    Then("백테스트가 정상적으로 실행된다") {
//                        transactionTemplate.execute {
//                            // 백테스트 실행 시 예외가 발생하지 않아야 함
//                            backTestService.backTest(
//                                    testAlgorithm.id!!,
//                                    startDate,
//                                    endDate,
//                                    deposit
//                            )
//
//                            // 알고리즘 로그가 생성되었는지 확인
//                            val algorithmLogs =
//                                    algorithmRepository
//                                            .findById(testAlgorithm.id!!)
//                                            .get()
//                                            .algorithmLog
//                            algorithmLogs.size shouldNotBe 0
//
//                            // 거래 내역이 생성되었는지 확인
//                            val algorithmLog = algorithmLogs.first()
//
//                            // 알고리즘 로그 날짜 확인
//                            algorithmLog.algorithmLogDateList.size shouldNotBe 0
//
//                            // 알고리즘 로그 주식 거래 내역 확인
//                            algorithmLog.algorithmLogStockList.size shouldNotBe 0
//
//                            // 삼성전자 매수 거래가 있는지 확인
//                            val samsungBuyTrade =
//                                    algorithmLog.algorithmLogStockList.find {
//                                        it.stockCode == "005930" && it.orderType == OrderType.BUY
//                                    }
//                            samsungBuyTrade shouldNotBe null
//
//                            // 카카오 매수 거래가 있는지 확인
//                            val kakaoBuyTrade =
//                                    algorithmLog.algorithmLogStockList.find {
//                                        it.stockCode == "035720" && it.orderType == OrderType.BUY
//                                    }
//                            kakaoBuyTrade shouldNotBe null
//
//                            // 네이버 매수 거래가 있는지 확인
//                            val naverBuyTrade =
//                                    algorithmLog.algorithmLogStockList.find {
//                                        it.stockCode == "035420" && it.orderType == OrderType.BUY
//                                    }
//                            naverBuyTrade shouldNotBe null
//
//                            // 삼성전자 매도 거래가 있는지 확인
//                            val samsungSellTrade =
//                                    algorithmLog.algorithmLogStockList.find {
//                                        it.stockCode == "005930" && it.orderType == OrderType.SELL
//                                    }
//                            samsungSellTrade shouldNotBe null
//                            null
//                        }
//                    }
//                }

            When("존재하지 않는 알고리즘 ID로 백테스트 실행시") {
                val invalidAlgorithmId = -1L
                Then("리소스 없음 예외가 발생한다") {
                    val exception =
                        shouldThrow<ApiException> {
                            backTestService.backTest(
                                invalidAlgorithmId,
                                startDate,
                                endDate,
                                deposit
                            )
                        }
                    exception.responseCode shouldBe ResponseCode.RESOURCE_NOT_FOUND
                }
            }

            //                When("종료일이 시작일보다 이전인 경우") {
            //                    val invalidEndDate = LocalDate.of(2022, 12, 31)
            //                    Then("백테스트가 실행되지만 거래가 발생하지 않는다") {
            //                        // 백테스트 실행
            //                        backTestService.backTest(
            //                                testAlgorithm.id!!,
            //                                startDate,
            //                                invalidEndDate,
            //                                deposit
            //                        )
            //
            //                        // 알고리즘 로그가 생성되었는지 확인
            //                        val algorithmLogs =
            //
            // algorithmRepository.findById(testAlgorithm.id!!).get().algorithmLog
            //                        algorithmLogs.size shouldNotBe 0
            //
            //                        // 거래 내역이 없어야 함
            //                        algorithmLogs.forEach { log ->
            // log.algorithmLogStockList.size shouldBe 0 }
            //                    }
            //                }
        }
    })
