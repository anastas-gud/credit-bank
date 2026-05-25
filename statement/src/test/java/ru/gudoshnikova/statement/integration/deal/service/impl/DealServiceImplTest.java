package ru.gudoshnikova.statement.integration.deal.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import ru.gudoshnikova.statement.dto.LoanOfferDto;
import ru.gudoshnikova.statement.dto.LoanStatementRequestDto;
import ru.gudoshnikova.statement.util.ApiConstants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealServiceImplTest {
    @Mock
    private RestClient dealRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private DealServiceImpl dealService;

    private LoanStatementRequestDto loanStatementRequest;
    private LoanOfferDto loanOffer;
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

        loanOffer = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(BigDecimal.valueOf(300000))
                .totalAmount(BigDecimal.valueOf(300000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(26500))
                .rate(BigDecimal.valueOf(15.0))
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();

        loanOffers = Arrays.asList(loanOffer);
    }

    @Test
    @DisplayName("Отправка запроса на создание заявки - успешно")
    void sendStatementRequestSuccess() {
        when(dealRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LoanStatementRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(loanOffers);

        List<LoanOfferDto> result = dealService.sendStatementRequest(loanStatementRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(loanOffer.getRate(), result.get(0).getRate());

        verify(dealRestClient, times(1)).post();
        verify(requestBodyUriSpec, times(1)).uri(ApiConstants.DEAL_STATEMENT);
        verify(requestBodySpec, times(1)).body(loanStatementRequest);
        verify(responseSpec, times(1)).body(any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Отправка запроса на создание заявки - ошибка сервиса")
    void sendStatementRequestServiceFails() {
        when(dealRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LoanStatementRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Deal service unavailable"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> dealService.sendStatementRequest(loanStatementRequest)
        );

        assertTrue(exception.getMessage().contains("Deal service unavailable"));
        verify(dealRestClient, times(1)).post();
    }

    @Test
    @DisplayName("Отправка запроса на выбор предложения - успешно")
    void sendSelectOfferRequestSuccess() {
        when(dealRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LoanOfferDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        assertDoesNotThrow(() -> dealService.sendSelectOfferRequest(loanOffer));

        verify(dealRestClient, times(1)).post();
        verify(requestBodyUriSpec, times(1)).uri(ApiConstants.DEAL_OFFER_SELECT);
        verify(requestBodySpec, times(1)).body(loanOffer);
        verify(responseSpec, times(1)).toBodilessEntity();
    }

    @Test
    @DisplayName("Отправка запроса на выбор предложения - ошибка сервиса")
    void sendSelectOfferRequestServiceFails() {
        when(dealRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LoanOfferDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Deal service error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> dealService.sendSelectOfferRequest(loanOffer)
        );

        assertTrue(exception.getMessage().contains("Deal service error"));
        verify(dealRestClient, times(1)).post();
    }
}