package ru.gudoshnikova.deal.integration.calculator.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import ru.gudoshnikova.deal.dto.CreditDto;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.LoanStatementRequestDto;
import ru.gudoshnikova.deal.dto.PaymentScheduleElementDto;
import ru.gudoshnikova.deal.dto.ScoringDataDto;
import ru.gudoshnikova.deal.enums.Gender;
import ru.gudoshnikova.deal.util.ApiConstants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalculatorServiceImplTest {
    @Mock
    private RestClient calculatorRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private CalculatorServiceImpl calculatorService;

    private LoanStatementRequestDto loanStatementRequest;
    private ScoringDataDto scoringDataDto;
    private CreditDto creditDto;
    private List<LoanOfferDto> loanOffers;

    @BeforeEach
    void setUp() {
        loanStatementRequest = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivan@mail.ru")
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("567890")
                .build();

        scoringDataDto = ScoringDataDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .gender(Gender.MALE)
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();

        creditDto = CreditDto.builder()
                .amount(BigDecimal.valueOf(309000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(27300.25))
                .rate(BigDecimal.valueOf(12.0))
                .psk(BigDecimal.valueOf(12.5))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .paymentSchedule(List.of(PaymentScheduleElementDto.builder()
                        .number(1)
                        .date(LocalDate.now().plusMonths(1))
                        .totalPayment(BigDecimal.valueOf(27300.25))
                        .interestPayment(BigDecimal.valueOf(3090))
                        .debtPayment(BigDecimal.valueOf(24210.25))
                        .remainingDebt(BigDecimal.valueOf(284789.75))
                        .build()))
                .build();

        LoanOfferDto loanOffer = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(BigDecimal.valueOf(300000))
                .totalAmount(BigDecimal.valueOf(309000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(27300.25))
                .rate(BigDecimal.valueOf(12.0))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        loanOffers = Arrays.asList(loanOffer);
    }

    @Test
    @DisplayName("Отправка запроса на получение предложений - успешно")
    void sendOffersRequestSuccess() {
        when(calculatorRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LoanStatementRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(loanOffers);

        List<LoanOfferDto> result = calculatorService.sendOffersRequest(loanStatementRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(12.0), result.get(0).getRate());

        verify(calculatorRestClient, times(1)).post();
        verify(requestBodyUriSpec, times(1)).uri(ApiConstants.CALCULATOR_OFFERS);
        verify(requestBodySpec, times(1)).body(loanStatementRequest);
        verify(responseSpec, times(1)).body(any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Отправка запроса на получение предложений - ошибка сервиса")
    void sendOffersRequestServiceFails() {
        when(calculatorRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LoanStatementRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Calculator service unavailable"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> calculatorService.sendOffersRequest(loanStatementRequest)
        );

        assertTrue(exception.getMessage().contains("Calculator service unavailable"));
        verify(calculatorRestClient, times(1)).post();
    }

    @Test
    @DisplayName("Отправка запроса на расчет кредита - успешно")
    void sendCalculateRequestSuccess() {
        when(calculatorRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ScoringDataDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CreditDto.class)).thenReturn(creditDto);

        CreditDto result = calculatorService.sendCalculateRequest(scoringDataDto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(12.0), result.getRate());
        assertEquals(BigDecimal.valueOf(309000), result.getAmount());

        verify(calculatorRestClient, times(1)).post();
        verify(requestBodyUriSpec, times(1)).uri(ApiConstants.CALCULATOR_CALC);
        verify(requestBodySpec, times(1)).body(scoringDataDto);
        verify(responseSpec, times(1)).body(CreditDto.class);
    }

    @Test
    @DisplayName("Отправка запроса на расчет кредита - ошибка сервиса")
    void sendCalculateRequestServiceFails() {
        when(calculatorRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ScoringDataDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Calculator service error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> calculatorService.sendCalculateRequest(scoringDataDto)
        );

        assertTrue(exception.getMessage().contains("Calculator service error"));
        verify(calculatorRestClient, times(1)).post();
    }
}
