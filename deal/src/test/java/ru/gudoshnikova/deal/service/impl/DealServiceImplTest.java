package ru.gudoshnikova.deal.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.gudoshnikova.deal.dto.EmailMessage;
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
import ru.gudoshnikova.deal.dto.CreditDto;
import ru.gudoshnikova.deal.dto.ScoringDataDto;
import ru.gudoshnikova.deal.exception.CalculatorServiceException;
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
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private DocumentGeneratorServiceImpl documentGenerator;

    @Mock
    private KafkaTemplate<String, EmailMessage> kafkaTemplate;

    private LoanStatementRequestDto loanStatementRequest;
    private LoanOfferDto loanOffer;
    private FinishRegistrationRequestDto finishRegistrationRequest;
    private Client client;
    private Statement statement;
    private Credit credit;
    private CreditDto creditDto;
    private ScoringDataDto scoringDataDto;
    private List<LoanOfferDto> loanOffers;
    private byte[] testDocument;

    @BeforeEach
    void setUp() {
        testDocument = "test document content".getBytes();

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

        loanOffer = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(BigDecimal.valueOf(300000))
                .totalAmount(BigDecimal.valueOf(309000))
                .term(12)
                .monthlyPayment(BigDecimal.valueOf(27300.25))
                .rate(BigDecimal.valueOf(12.0))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        statement = Statement.builder()
                .statementId(UUID.randomUUID())
                .client(client)
                .status(ApplicationStatus.PREAPPROVAL)
                .creationDate(LocalDateTime.now())
                .statusHistory(new ArrayList<>())
                .appliedOffer(loanOffer)
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
        when(kafkaTemplate.send(anyString(), any(EmailMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> dealService.selectOffer(loanOffer));

        assertEquals(ApplicationStatus.APPROVED, statement.getStatus());
        assertNotNull(statement.getAppliedOffer());
        assertEquals(loanOffer, statement.getAppliedOffer());
        assertFalse(statement.getStatusHistory().isEmpty());

        verify(statementRepository, times(1)).findByIdWithLock(loanOffer.getStatementId());
        verify(statementRepository, times(1)).save(statement);
        verify(kafkaTemplate, times(1)).send(eq("finish-registration"), any(EmailMessage.class));
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
        when(kafkaTemplate.send(anyString(), any(EmailMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

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
        verify(kafkaTemplate, times(1)).send(eq("create-documents"), any(EmailMessage.class));
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

    @Test
    void sendDocumentsSuccess() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(statement));
        when(documentGenerator.generateCreditDocument(any(), any(), anyDouble(), anyInt(), anyDouble()))
                .thenReturn(testDocument);
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);
        when(kafkaTemplate.send(anyString(), any(EmailMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> dealService.sendDocuments(statement.getStatementId()));

        assertEquals(ApplicationStatus.PREPARE_DOCUMENTS, statement.getStatus());
        assertFalse(statement.getStatusHistory().isEmpty());

        verify(statementRepository, times(1)).findById(statement.getStatementId());
        verify(documentGenerator, times(1)).generateCreditDocument(
                eq(statement.getStatementId()),
                eq(client.getFirstName() + " " + client.getLastName()),
                eq(loanOffer.getTotalAmount().doubleValue()),
                eq(loanOffer.getTerm()),
                eq(loanOffer.getRate().doubleValue())
        );
        verify(statementRepository, times(1)).save(statement);
        verify(kafkaTemplate, times(1)).send(eq("send-documents"), any(EmailMessage.class));
    }

    @Test
    void sendDocumentsStatementNotFound() {
        UUID statementId = UUID.randomUUID();
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> dealService.sendDocuments(statementId));

        assertTrue(exception.getMessage().contains("Statement not found"));
        verify(statementRepository, times(1)).findById(statementId);
        verify(documentGenerator, never()).generateCreditDocument(any(), any(), anyDouble(), anyInt(), anyDouble());
        verify(statementRepository, never()).save(any());
    }

    @Test
    void signDocumentsSuccess() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(statement));
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);
        when(kafkaTemplate.send(anyString(), any(EmailMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        ArgumentCaptor<Statement> statementCaptor = ArgumentCaptor.forClass(Statement.class);

        assertDoesNotThrow(() -> dealService.signDocuments(statement.getStatementId()));

        verify(statementRepository, times(1)).findById(statement.getStatementId());
        verify(statementRepository, times(1)).save(statementCaptor.capture());
        verify(kafkaTemplate, times(1)).send(eq("send-ses"), any(EmailMessage.class));

        Statement savedStatement = statementCaptor.getValue();
        assertNotNull(savedStatement.getSesCode());
        assertEquals(6, savedStatement.getSesCode().length());
        assertTrue(savedStatement.getSesCode().matches("\\d{6}"));
    }

    @Test
    void signDocumentsStatementNotFound() {
        UUID statementId = UUID.randomUUID();
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> dealService.signDocuments(statementId));

        assertTrue(exception.getMessage().contains("Statement not found"));
        verify(statementRepository, times(1)).findById(statementId);
        verify(statementRepository, never()).save(any());
    }

    @Test
    void verifyCodeSuccess() {
        String correctCode = "123456";
        statement.setSesCode(correctCode);
        statement.setCredit(credit);

        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(statement));
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);
        when(kafkaTemplate.send(anyString(), any(EmailMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> dealService.verifyCode(statement.getStatementId(), correctCode));

        assertEquals(CreditStatus.ISSUED, credit.getCreditStatus());
        assertEquals(ApplicationStatus.CREDIT_ISSUED, statement.getStatus());
        assertNotNull(statement.getSignDate());
        assertFalse(statement.getStatusHistory().isEmpty());

        verify(statementRepository, times(1)).findById(statement.getStatementId());
        verify(creditRepository, times(1)).save(credit);
        verify(statementRepository, times(1)).save(statement);
        verify(kafkaTemplate, times(1)).send(eq("credit-issued"), any(EmailMessage.class));
    }

    @Test
    void verifyCodeStatementNotFound() {
        UUID statementId = UUID.randomUUID();
        String code = "123456";
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> dealService.verifyCode(statementId, code));

        assertTrue(exception.getMessage().contains("Statement not found"));
        verify(statementRepository, times(1)).findById(statementId);
        verify(creditRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
    }

    @Test
    void verifyCodeInvalidCode() {
        statement.setSesCode("123456");
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(statement));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dealService.verifyCode(statement.getStatementId(), "000000"));

        assertEquals("Invalid verification code", exception.getMessage());
        verify(statementRepository, times(1)).findById(statement.getStatementId());
        verify(creditRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
    }

    @Test
    void verifyCodeNullCodeInStatement() {
        statement.setSesCode(null);
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(statement));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dealService.verifyCode(statement.getStatementId(), "123456"));

        assertEquals("Invalid verification code", exception.getMessage());
        verify(statementRepository, times(1)).findById(statement.getStatementId());
        verify(creditRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
    }

    @Test
    void calculateCreditCalculatorDenied() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(statement));
        doNothing().when(clientMapper).updateClientFromFinishRegistration(any(), any());
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(scoringDataMapper.toScoringDataDto(any(Client.class), any(Statement.class)))
                .thenReturn(scoringDataDto);
        when(calculatorService.sendCalculateRequest(any(ScoringDataDto.class)))
                .thenThrow(new CalculatorServiceException("Loan denied: insufficient income", "LOAN_DENIED"));
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);
        when(kafkaTemplate.send(anyString(), any(EmailMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> dealService.calculateCredit(statement.getStatementId(), finishRegistrationRequest));

        assertEquals(ApplicationStatus.CC_DENIED, statement.getStatus());
        verify(statementRepository, times(1)).save(statement);
        verify(kafkaTemplate, times(1)).send(eq("statement-denied"), any(EmailMessage.class));
    }
}