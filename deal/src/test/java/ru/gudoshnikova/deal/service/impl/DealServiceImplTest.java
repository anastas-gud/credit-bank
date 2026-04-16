package ru.gudoshnikova.deal.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import ru.gudoshnikova.deal.dto.EmploymentDto;
import ru.gudoshnikova.deal.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.LoanStatementRequestDto;
import ru.gudoshnikova.deal.dto.PaymentScheduleElementDto;
import ru.gudoshnikova.deal.enums.ApplicationStatus;
import ru.gudoshnikova.deal.enums.Gender;
import ru.gudoshnikova.deal.enums.MaritalStatus;
import ru.gudoshnikova.deal.enums.EmploymentStatus;
import ru.gudoshnikova.deal.enums.EmploymentPosition;
import ru.gudoshnikova.deal.enums.CreditStatus;
import ru.gudoshnikova.deal.config.RestClientConfig;
import ru.gudoshnikova.deal.dto.CreditDto;
import ru.gudoshnikova.deal.dto.ScoringDataDto;
import ru.gudoshnikova.deal.exception.NotFoundException;
import ru.gudoshnikova.deal.integration.calculator.service.CalculatorService;
import ru.gudoshnikova.deal.mapper.ClientMapper;
import ru.gudoshnikova.deal.mapper.CreditMapper;
import ru.gudoshnikova.deal.mapper.ScoringDataMapper;
import ru.gudoshnikova.deal.model.Client;
import ru.gudoshnikova.deal.model.Credit;
import ru.gudoshnikova.deal.model.Statement;
import ru.gudoshnikova.deal.repository.ClientRepository;
import ru.gudoshnikova.deal.repository.CreditRepository;
import ru.gudoshnikova.deal.repository.StatementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealServiceImplTest {

    @InjectMocks
    private DealServiceImpl dealService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private CalculatorService calculatorService;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private ScoringDataMapper scoringDataMapper;

    @Mock
    private CreditMapper creditMapper;

    private LoanStatementRequestDto loanStatementRequest;
    private LoanOfferDto loanOffer;
    private FinishRegistrationRequestDto finishRegistrationRequest;
    private Client client;
    private Statement statement;
    private Credit credit;
    private CreditDto creditDto;
    private ScoringDataDto scoringDataDto;
    private List<LoanOfferDto> loanOffers;

    @BeforeEach
    void setUp() {
        loanStatementRequest = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .email("ivan@mail.ru")
                .birthdate(LocalDate.of(1996, 12, 23))
                .passportSeries("3756")
                .passportNumber("127539")
                .build();

        client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivan@mail.ru")
                .birthdate(LocalDate.of(1996, 12, 23))
                .build();

        statement = Statement.builder()
                .statementId(UUID.randomUUID())
                .client(client)
                .status(ApplicationStatus.PREAPPROVAL)
                .creationDate(LocalDateTime.now())
                .statusHistory(new ArrayList<>())
                .build();

        loanOffer = LoanOfferDto.builder()
                .statementId(statement.getStatementId())
                .requestedAmount(BigDecimal.valueOf(300000))
                .totalAmount(BigDecimal.valueOf(309000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(27300.25))
                .rate(BigDecimal.valueOf(12.0))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        loanOffers = Arrays.asList(loanOffer);

        finishRegistrationRequest = FinishRegistrationRequestDto.builder()
                .gender(Gender.MALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(2)
                .passportIssueDate(LocalDate.of(2010, 5, 15))
                .passportIssueBranch("770-001")
                .employment(EmploymentDto.builder()
                        .employmentStatus(EmploymentStatus.EMPLOYED)
                        .employerINN("7701234567")
                        .salary(BigDecimal.valueOf(100000))
                        .position(EmploymentPosition.MIDDLE_MANAGER)
                        .workExperienceTotal(60)
                        .workExperienceCurrent(24)
                        .build())
                .accountNumber("40817810000012345678")
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

        scoringDataDto = ScoringDataDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(12)
                .firstName("Иван")
                .lastName("Иванов")
                .gender(Gender.MALE)
                .build();

        credit = Credit.builder()
                .creditId(UUID.randomUUID())
                .statement(statement)
                .amount(BigDecimal.valueOf(309000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(27300.25))
                .rate(BigDecimal.valueOf(12.0))
                .psk(BigDecimal.valueOf(12.5))
                .creditStatus(CreditStatus.CALCULATED)
                .build();
    }

    @Test
    void createStatementSuccess() {
        when(clientMapper.toClient(any(LoanStatementRequestDto.class))).thenReturn(client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(statementRepository.save(any(Statement.class))).thenAnswer(invocation -> {
            Statement savedStatement = invocation.getArgument(0);
            savedStatement.setStatementId(statement.getStatementId());
            return savedStatement;
        });
        when(calculatorService.sendOffersRequest(any(LoanStatementRequestDto.class)))
                .thenReturn(loanOffers);

        List<LoanOfferDto> result = dealService.createStatement(loanStatementRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(statement.getStatementId(), result.get(0).getStatementId());

        verify(clientMapper, times(1)).toClient(loanStatementRequest);
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(statementRepository, times(1)).save(any(Statement.class));
        verify(calculatorService, times(1)).sendOffersRequest(loanStatementRequest);
    }

    @Test
    void selectOfferSuccess() {
        when(statementRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(statement));
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);

        assertDoesNotThrow(() -> dealService.selectOffer(loanOffer));

        assertEquals(ApplicationStatus.APPROVED, statement.getStatus());
        assertNotNull(statement.getAppliedOffer());
        assertEquals(loanOffer, statement.getAppliedOffer());
        assertFalse(statement.getStatusHistory().isEmpty());

        verify(statementRepository, times(1)).findByIdWithLock(loanOffer.getStatementId());
        verify(statementRepository, times(1)).save(statement);
    }

    @Test
    void selectOfferStatementNotFound() {
        when(statementRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> dealService.selectOffer(loanOffer));

        assertTrue(exception.getMessage().contains("Statement not found"));
        verify(statementRepository, times(1)).findByIdWithLock(loanOffer.getStatementId());
        verify(statementRepository, never()).save(any());
    }

    @Test
    void calculateCreditSuccess() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(statement));
        doNothing().when(clientMapper).updateClientFromFinishRegistration(any(), any());
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(scoringDataMapper.toScoringDataDto(any(Client.class), any(Statement.class)))
                .thenReturn(scoringDataDto);
        when(calculatorService.sendCalculateRequest(any(ScoringDataDto.class)))
                .thenReturn(creditDto);
        when(creditMapper.toCredit(any(CreditDto.class), any(Statement.class))).thenReturn(credit);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);

        assertDoesNotThrow(() -> dealService.calculateCredit(statement.getStatementId(), finishRegistrationRequest));

        assertEquals(ApplicationStatus.CC_APPROVED, statement.getStatus());
        assertNotNull(statement.getCredit());

        verify(statementRepository, times(1)).findById(statement.getStatementId());
        verify(clientMapper, times(1)).updateClientFromFinishRegistration(any(), any());
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(scoringDataMapper, times(1)).toScoringDataDto(any(), any());
        verify(calculatorService, times(1)).sendCalculateRequest(any(ScoringDataDto.class));
        verify(creditMapper, times(1)).toCredit(any(), any());
        verify(creditRepository, times(1)).save(any(Credit.class));
        verify(statementRepository, times(1)).save(statement);
    }

    @Test
    void calculateCreditStatementNotFound() {
        UUID statementId = UUID.randomUUID();
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> dealService.calculateCredit(statementId, finishRegistrationRequest));

        assertTrue(exception.getMessage().contains("Statement not found"));
        verify(statementRepository, times(1)).findById(statementId);
        verify(clientMapper, never()).updateClientFromFinishRegistration(any(), any());
        verify(creditRepository, never()).save(any());
    }
}