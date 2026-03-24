package ru.gudoshnikova.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gudoshnikova.calculator.api.dto.CreditDto;
import ru.gudoshnikova.calculator.api.dto.EmploymentDto;
import ru.gudoshnikova.calculator.api.dto.LoanOfferDto;
import ru.gudoshnikova.calculator.api.dto.LoanStatementRequestDto;
import ru.gudoshnikova.calculator.api.dto.ScoringDataDto;
import ru.gudoshnikova.calculator.api.enums.EmploymentStatus;
import ru.gudoshnikova.calculator.api.enums.Gender;
import ru.gudoshnikova.calculator.api.enums.MaritalStatus;
import ru.gudoshnikova.calculator.api.enums.Position;
import ru.gudoshnikova.calculator.config.CalculatorConfig;
import ru.gudoshnikova.calculator.service.impl.CalculatorServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculatorServiceImplTest {

    @InjectMocks
    private CalculatorServiceImpl calculatorService;

    @Mock
    private PrescoringService prescoringService;

    @Mock
    private ScoringService scoringService;

    @Mock
    private CalculatorConfig config;

    private LoanStatementRequestDto loanStatementRequestDto;
    private ScoringDataDto scoringDataDto;

    @BeforeEach
    void setUp() {
        lenient().when(config.getBaseRate()).thenReturn(BigDecimal.valueOf(15.0).setScale(2, RoundingMode.HALF_UP));
        lenient().when(config.getInsuranceCostPercent()).thenReturn(BigDecimal.valueOf(3.0));

        loanStatementRequestDto = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(24)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .email("ivan@mail.ru")
                .birthdate(LocalDate.of(2000, 1, 1))
                .passportSeries("3452")
                .passportNumber("523698")
                .build();

        EmploymentDto employmentDto = EmploymentDto.builder()
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .employerINN("7701234567")
                .salary(BigDecimal.valueOf(100000))
                .position(Position.WORKER)
                .workExperienceTotal(60)
                .workExperienceCurrent(24)
                .build();

        scoringDataDto = ScoringDataDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(24)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .gender(Gender.MALE)
                .birthdate(LocalDate.of(2000, 1, 1))
                .passportSeries("5648")
                .passportNumber("83629")
                .passportIssueDate(LocalDate.of(2025, 1, 15))
                .passportIssueBranch("770-001")
                .maritalStatus(MaritalStatus.SINGLE)
                .dependentAmount(0)
                .employment(employmentDto)
                .accountNumber("40817810000012345678")
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();
    }

    @Test
    @DisplayName("Успешный расчет кредитных предложений")
    void calculateLoanOffersSuccess() {
        List<LoanOfferDto> offers = calculatorService.calculateLoanOffers(loanStatementRequestDto);

        assertNotNull(offers);
        assertEquals(4, offers.size());

        verify(prescoringService, times(1)).prescoring(loanStatementRequestDto);

        for (int i = 0; i < offers.size() - 1; i++) {
            assertTrue(offers.get(i).getRate().compareTo(offers.get(i + 1).getRate()) >= 0);
        }
    }

    @Test
    @DisplayName("Расчет кредита с учетом отсутствия страховки, проверка суммы кредита и ставки")
    void calculateCreditWithoutInsurance() {
        when(scoringService.score(any(ScoringDataDto.class))).thenReturn(BigDecimal.valueOf(15));
        CreditDto creditDto = calculatorService.calculateCredit(scoringDataDto);

        assertNotNull(creditDto);
        assertEquals(creditDto.getAmount(), scoringDataDto.getAmount());
        assertEquals(creditDto.getRate(), BigDecimal.valueOf(15));
        assertFalse(creditDto.getIsInsuranceEnabled());
        assertFalse(creditDto.getIsSalaryClient());
    }

    @Test
    @DisplayName("Расчет кредита с учетом страховки, проверка суммы кредита и ставки")
    void calculateCreditWithInsurance() {
        scoringDataDto.setIsInsuranceEnabled(true);
        when(scoringService.score(any(ScoringDataDto.class))).thenReturn(BigDecimal.valueOf(15.0));

        CreditDto credit = calculatorService.calculateCredit(scoringDataDto);

        assertNotNull(credit);
        BigDecimal expectedInsuranceCost = scoringDataDto.getAmount()
                .multiply(config.getInsuranceCostPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal expectedAmount = scoringDataDto.getAmount().add(expectedInsuranceCost);

        assertEquals(expectedAmount, credit.getAmount());
        assertEquals(BigDecimal.valueOf(15.0), credit.getRate());
        assertTrue(credit.getIsInsuranceEnabled());
        assertFalse(credit.getIsSalaryClient());
    }

    @Test
    @DisplayName("Расчет кредита с учетом зарплатного клиента, проверка суммы кредита и ставки")
    void calculateCreditWithSalaryClient() {
        scoringDataDto.setIsSalaryClient(true);
        when(scoringService.score(any(ScoringDataDto.class))).thenReturn(BigDecimal.valueOf(15.0));

        CreditDto credit = calculatorService.calculateCredit(scoringDataDto);

        assertNotNull(credit);
        assertEquals(scoringDataDto.getAmount(), credit.getAmount());
        assertEquals(BigDecimal.valueOf(15.0), credit.getRate());
        assertFalse(credit.getIsInsuranceEnabled());
        assertTrue(credit.getIsSalaryClient());
    }

    @Test
    @DisplayName("Расчет ежемесячных выплат")
    void calculateMonthlyPayment() {
        int term = 24;

        List<LoanOfferDto> offers = calculatorService.calculateLoanOffers(loanStatementRequestDto);

        assertNotNull(offers);
        assertEquals(4, offers.size());

        for (LoanOfferDto offer : offers) {
            BigDecimal monthlyPayment = offer.getMonthlyPayment();
            assertNotNull(monthlyPayment);
            assertTrue(monthlyPayment.compareTo(BigDecimal.ZERO) > 0);

            assertTrue(monthlyPayment.compareTo(offer.getTotalAmount()) < 0);

            BigDecimal calculatedTotal = monthlyPayment
                    .multiply(BigDecimal.valueOf(term))
                    .setScale(2, RoundingMode.HALF_UP);

            assertTrue(calculatedTotal.compareTo(offer.getTotalAmount()) > 0);
        }
    }
}