package ru.gudoshnikova.statement.service.impl;

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
import ru.gudoshnikova.statement.exception.PrescoringFailedException;
import ru.gudoshnikova.statement.service.PrescoringService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceImplTest {

    @Mock
    private PrescoringService prescoringService;

    @Mock
    private RestClient dealRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private StatementServiceImpl statementService;

    private LoanStatementRequestDto validLoanStatementRequest;
    private LoanOfferDto loanOffer1;
    private LoanOfferDto loanOffer2;
    private List<LoanOfferDto> loanOffers;

    @BeforeEach
    void setUp() {
        validLoanStatementRequest = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .email("ivan@mail.ru")
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("567890")
                .build();

        loanOffer1 = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(BigDecimal.valueOf(300000))
                .totalAmount(BigDecimal.valueOf(300000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(26500))
                .rate(BigDecimal.valueOf(15.0))
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();

        loanOffer2 = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(BigDecimal.valueOf(300000))
                .totalAmount(BigDecimal.valueOf(309000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(27300))
                .rate(BigDecimal.valueOf(12.0))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        loanOffers = Arrays.asList(loanOffer1, loanOffer2);
    }

    @Test
    @DisplayName("Создание заявки - успешный сценарий")
    void createStatementSuccess() {
        doNothing().when(prescoringService).prescoring(any(LoanStatementRequestDto.class));

        when(dealRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LoanStatementRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(loanOffers);

        List<LoanOfferDto> result = statementService.createStatement(validLoanStatementRequest);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(loanOffer1.getRate(), result.get(0).getRate());
        assertEquals(loanOffer2.getRate(), result.get(1).getRate());

        verify(prescoringService, times(1)).prescoring(validLoanStatementRequest);
        verify(dealRestClient, times(1)).post();
    }

    @Test
    @DisplayName("Создание заявки - ошибка прескоринга")
    void createStatementPrescoringFails() {
        doThrow(new PrescoringFailedException("Age must be at least 18 years old"))
                .when(prescoringService).prescoring(any(LoanStatementRequestDto.class));

        PrescoringFailedException exception = assertThrows(
                PrescoringFailedException.class,
                () -> statementService.createStatement(validLoanStatementRequest)
        );

        assertEquals("Age must be at least 18 years old", exception.getMessage());
        verify(prescoringService, times(1)).prescoring(validLoanStatementRequest);
        verify(dealRestClient, never()).post();
    }

    @Test
    @DisplayName("Выбор предложения - успешный сценарий")
    void selectOfferSuccess() {
        when(dealRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LoanOfferDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        assertDoesNotThrow(() -> statementService.selectOffer(loanOffer1));

        verify(dealRestClient, times(1)).post();
        verify(requestBodyUriSpec, times(1)).uri("/deal/offer/select");
        verify(requestBodySpec, times(1)).body(loanOffer1);
        verify(responseSpec, times(1)).toBodilessEntity();
    }
}