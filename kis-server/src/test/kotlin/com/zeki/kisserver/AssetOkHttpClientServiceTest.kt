package com.zeki.kisserver

import com.zeki.kisserver.domain.kis.stock_info.AssetConnectService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [KisVolKotlinApplication::class]) // 애플리케이션 클래스 지정
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssetOkHttpClientServiceTest @Autowired constructor(
    private val assetConnectService: AssetConnectService
) {

    @Test
    fun `getAccountData 호출 시 결과 리스트가 비어 있는지 확인`() {
//        val result = assetWebClientService.getAccountData()
//
//        // 리스트의 크기가 0인지 검증
//        assertThat(result.size).isEqualTo(0)
    }
}

