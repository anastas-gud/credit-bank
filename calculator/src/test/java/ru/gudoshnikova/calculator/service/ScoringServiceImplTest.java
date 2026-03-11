package ru.gudoshnikova.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gudoshnikova.calculator.config.CalculatorConfig;
import ru.gudoshnikova.calculator.dto.EmploymentDto;
import ru.gudoshnikova.calculator.dto.ScoringDataDto;
import ru.gudoshnikova.calculator.enums.EmploymentStatus;
import ru.gudoshnikova.calculator.enums.Gender;
import ru.gudoshnikova.calculator.enums.MaritalStatus;
import ru.gudoshnikova.calculator.enums.Position;
import ru.gudoshnikova.calculator.exception.LoanDeniedException;
import ru.gudoshnikova.calculator.service.impl.ScoringServiceImpl;
import ru.gudoshnikova.calculator.util.ScoringConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ScoringServiceImplTest {

    @InjectMocks
    private ScoringServiceImpl scoringService;

    @Mock
    private CalculatorConfig config;

    @Mock
    private ScoringDataDto scoringDataDto;

    private EmploymentDto employmentDto;

    @BeforeEach
    void setUp() {
        lenient().when(config.getBaseRate()).thenReturn(BigDecimal.valueOf(15));

        employmentDto = EmploymentDto.builder()
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
                .maritalStatus(MaritalStatus.SINGLE)
                .dependentAmount(2)
                .employment(employmentDto)
                .accountNumber("40817810000012345678")
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();
    }

    @Test
    @DisplayName("Отказ в выдаче кредита при слишком маленьком возрасте")
    void scoreLoanDeniedWhenAgeTooSmall() {
        scoringDataDto.setBirthdate(LocalDate.now().minusYears(ScoringConstants.MIN_AGE - 1));
        assertThrows(LoanDeniedException.class,
                () -> scoringService.score(scoringDataDto));
    }

    @Test
    @DisplayName("Отказ в выдаче кредита при слишком большом возрасте")
    void scoreLoanDeniedWhenAgeTooBig() {
        scoringDataDto.setBirthdate(LocalDate.now().minusYears(ScoringConstants.MAX_AGE + 1));
        assertThrows(LoanDeniedException.class,
                () -> scoringService.score(scoringDataDto));
    }

    @Test
    @DisplayName("Отказ в выдаче кредита при статусе 'Безработный'")
    void scoreLoanDeniedWhenStatusUnemployed() {
        employmentDto.setEmploymentStatus(EmploymentStatus.UNEMPLOYED);
        assertThrows(LoanDeniedException.class,
                () -> scoringService.score(scoringDataDto));
    }

    @Test
    @DisplayName("Отказ в выдаче кредита при превышении суммы кредита n зарплат")
    void scoreLoanDeniedWhenLoanMoreSalary() {
        BigDecimal salary = BigDecimal.valueOf(100000);
        BigDecimal amount = salary
                .multiply(BigDecimal.valueOf(ScoringConstants.MAX_LOAN_TO_SALARY_RATIO))
                .add(BigDecimal.ONE);

        scoringDataDto.setAmount(amount);
        employmentDto.setSalary(salary);
        assertThrows(LoanDeniedException.class,
                () -> scoringService.score(scoringDataDto));
    }

    @Test
    @DisplayName("Отказ в выдаче кредита при недостаточным опыте работы")
    void scoreLoanDeniedWhenTotalWorkExperienceTooSmall() {
        employmentDto.setWorkExperienceTotal(ScoringConstants.MIN_TOTAL_EXPERIENCE - 1);
        assertThrows(LoanDeniedException.class,
                () -> scoringService.score(scoringDataDto));
    }

    @Test
    @DisplayName("Отказ в выдаче кредита при недостаточным текущем опыте работы")
    void scoreLoanDeniedWhenCurrentWorkExperienceTooSmall() {
        employmentDto.setWorkExperienceCurrent(ScoringConstants.MIN_CURRENT_EXPERIENCE - 1);
        assertThrows(LoanDeniedException.class,
                () -> scoringService.score(scoringDataDto));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом статуса работы 'Самозанятый'")
    void scoreRecalculationForEmploymentStatusWithSelfEmployed() {
        employmentDto.setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).add(ScoringConstants.SELF_EMPLOYED_INCREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом статуса работы 'Владелец бизнеса'")
    void scoreRecalculationForEmploymentStatusWithBusinessOwner() {
        employmentDto.setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).add(ScoringConstants.BUSINESS_OWNER_INCREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом позиции на работе 'Менеджер среднего звена'")
    void scoreRecalculationForPositionWithMiddleManager() {
        employmentDto.setPosition(Position.MIDDLE_MANAGER);

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).subtract(ScoringConstants.MID_MANAGER_DECREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом позиции на работе 'Топ-менеджер'")
    void scoreRecalculationForPositionWithTopManager() {
        employmentDto.setPosition(Position.TOP_MANAGER);

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).subtract(ScoringConstants.TOP_MANAGER_DECREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом семейного положения 'Женат/Замужем'")
    void scoreRecalculationForMaritalStatusWithMarried() {
        scoringDataDto.setMaritalStatus(MaritalStatus.MARRIED);

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).subtract(ScoringConstants.MARRIED_DECREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом семейного положения 'Разведен/Разведена'")
    void scoreRecalculationForMaritalStatusWithDivorced() {
        scoringDataDto.setMaritalStatus(MaritalStatus.DIVORCED);

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).add(ScoringConstants.DIVORCED_INCREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом женского пола и возраста")
    void scoreRecalculationForGenderAndAgeWithFemale() {
        scoringDataDto.setGender(Gender.FEMALE);
        scoringDataDto.setBirthdate(LocalDate.now().minusYears(ScoringConstants.FEMALE_AGE_MIN + 1));

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).subtract(ScoringConstants.FEMALE_DECREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом мужского пола и возраста")
    void scoreRecalculationForGenderAndAgeWithMale() {
        scoringDataDto.setGender(Gender.MALE);
        scoringDataDto.setBirthdate(LocalDate.now().minusYears(ScoringConstants.MALE_AGE_MIN + 1));

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).subtract(ScoringConstants.MALE_DECREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом не бинарного пола")
    void scoreRecalculationForGenderAndAgeWithNonBinary() {
        scoringDataDto.setGender(Gender.NON_BINARY);

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).add(ScoringConstants.NON_BINARY_INCREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом страховки")
    void scoreRecalculationForServicesWithInsurance() {
        scoringDataDto.setIsInsuranceEnabled(true);

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).subtract(ScoringConstants.INSURANCE_RATE_DECREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Проверка перерасчета ставки с учетом зарплатного клиента")
    void scoreRecalculationForServicesWithSalary() {
        scoringDataDto.setIsSalaryClient(true);

        BigDecimal resultRate = scoringService.score(scoringDataDto);
        BigDecimal expectedRate = BigDecimal.valueOf(15).subtract(ScoringConstants.SALARY_CLIENT_RATE_DECREASE);
        assertEquals(expectedRate.setScale(2, RoundingMode.HALF_UP),
                resultRate.setScale(2, RoundingMode.HALF_UP));
    }
}