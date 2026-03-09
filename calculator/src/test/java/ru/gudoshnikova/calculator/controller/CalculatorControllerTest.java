package ru.gudoshnikova.calculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.gudoshnikova.calculator.dto.CreditDto;
import ru.gudoshnikova.calculator.dto.EmploymentDto;
import ru.gudoshnikova.calculator.dto.LoanOfferDto;
import ru.gudoshnikova.calculator.dto.LoanStatementRequestDto;
import ru.gudoshnikova.calculator.dto.ScoringDataDto;
import ru.gudoshnikova.calculator.enums.EmploymentStatus;
import ru.gudoshnikova.calculator.enums.Gender;
import ru.gudoshnikova.calculator.enums.MaritalStatus;
import ru.gudoshnikova.calculator.enums.Position;
import ru.gudoshnikova.calculator.exception.LoanDeniedException;
import ru.gudoshnikova.calculator.service.CalculatorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalculatorController.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculatorService calculatorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Успешный расчет кредитных предложений")
    void calculateOffersSuccess() throws Exception {
        LoanStatementRequestDto request = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(24)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .email("ivan@mail.ru")
                .birthdate(LocalDate.of(2000, 1, 1))
                .passportSeries("3865")
                .passportNumber("175463")
                .build();

        LoanOfferDto offer = LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(BigDecimal.valueOf(300000))
                .totalAmount(BigDecimal.valueOf(300000))
                .term(24)
                .monthlyPayment(BigDecimal.valueOf(15000))
                .rate(BigDecimal.valueOf(15))
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();

        List<LoanOfferDto> offers = List.of(offer);

        Mockito.when(calculatorService.calculateLoanOffers(Mockito.any()))
                .thenReturn(offers);
        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalAmount").value(300000))
                .andExpect(jsonPath("$[0].rate").value(15));

    }

    @Test
    @DisplayName("Ошибка валидации (прескоринга) при расчете кредитных предложений")
    void calculateOffersValidationError() throws Exception {
        LoanStatementRequestDto request = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(24)
                .build();

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Успешный расчет полного кредита")
    void calculateCreditSuccess() throws Exception {
        EmploymentDto employment = EmploymentDto.builder()
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .employerINN("7701234567")
                .salary(BigDecimal.valueOf(100000))
                .position(Position.WORKER)
                .workExperienceTotal(60)
                .workExperienceCurrent(24)
                .build();

        ScoringDataDto request = ScoringDataDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(24)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .gender(Gender.MALE)
                .birthdate(LocalDate.of(2000, 1, 1))
                .passportSeries("5648")
                .passportNumber("836529")
                .passportIssueDate(LocalDate.of(2025, 1, 15))
                .passportIssueBranch("770-001")
                .maritalStatus(MaritalStatus.SINGLE)
                .dependentAmount(2)
                .employment(employment)
                .accountNumber("40817810000012345678")
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();

        CreditDto credit = CreditDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(24)
                .monthlyPayment(BigDecimal.valueOf(15000))
                .rate(BigDecimal.valueOf(15))
                .psk(BigDecimal.valueOf(15))
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .paymentSchedule(List.of())
                .build();

        Mockito.when(calculatorService.calculateCredit(Mockito.any()))
                .thenReturn(credit);

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(300000))
                .andExpect(jsonPath("$.rate").value(15));
    }

    @Test
    @DisplayName("Отказ в выдаче кредита")
    void calculateCreditLoanDenied() throws Exception {
        EmploymentDto employment = EmploymentDto.builder()
                .employmentStatus(EmploymentStatus.EMPLOYED)
                .employerINN("7701234567")
                .salary(BigDecimal.valueOf(1000))
                .position(Position.WORKER)
                .workExperienceTotal(60)
                .workExperienceCurrent(24)
                .build();

        ScoringDataDto request = ScoringDataDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(24)
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .gender(Gender.MALE)
                .birthdate(LocalDate.of(2000, 1, 1))
                .passportSeries("5648")
                .passportNumber("836529")
                .passportIssueDate(LocalDate.of(2025, 1, 15))
                .passportIssueBranch("770-001")
                .maritalStatus(MaritalStatus.SINGLE)
                .dependentAmount(2)
                .employment(employment)
                .accountNumber("40817810000012345678")
                .isInsuranceEnabled(false)
                .isSalaryClient(false)
                .build();

        Mockito.when(calculatorService.calculateCredit(Mockito.any()))
                .thenThrow(new LoanDeniedException("Отказ"));

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}