package com.zeki.kisvolkotlin.domain.data_go.holiday

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClient

@ExtendWith(MockitoExtension::class)
class HolidayTableEntityServiceTest {

    @Mock
    private lateinit var webClientDataGoMock: WebClient

    @InjectMocks
    private lateinit var holidayService: HolidayService

    @Test
    fun `getHolidaysFromDataGo returns HolidayResDto`() {
        // 가짜 WebClient.ResponseSpec 설정
        val uriSpecMock: WebClient.RequestHeadersUriSpec<*> = mock(WebClient.RequestHeadersUriSpec::class.java)
        val headersSpecMock: WebClient.RequestHeadersSpec<*> = mock(WebClient.RequestHeadersSpec::class.java)
        val responseSpecMock: WebClient.ResponseSpec = mock(WebClient.ResponseSpec::class.java)

//        `when`(webClientDataGoMock.get()).thenReturn(uriSpecMock)
//        `when`(uriSpecMock.uri(any(Function::class.java))).thenReturn(headersSpecMock)
//        `when`(headersSpecMock.exchangeToMono(any())).thenReturn(Mono.just(HolidayResDto())) // 가짜 응답 데이터
//
//        val result = holidayService.getHolidaysFromDataGo(LocalDate.now())
//
//        // 추가적인 결과 값 검증
    }
}