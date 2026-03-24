package ru.gudoshnikova.deal.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.gudoshnikova.deal.api.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.deal.api.dto.LoanOfferDto;
import ru.gudoshnikova.deal.api.dto.LoanStatementRequestDto;
import ru.gudoshnikova.deal.api.dto.EmploymentDto;
import ru.gudoshnikova.deal.api.enums.Gender;
import ru.gudoshnikova.deal.api.enums.MaritalStatus;
import ru.gudoshnikova.deal.api.enums.EmploymentPosition;
import ru.gudoshnikova.deal.api.enums.EmploymentStatus;
import ru.gudoshnikova.deal.exception.NotFoundException;
import ru.gudoshnikova.deal.service.DealService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DealControllerImpl.class)
class DealControllerImplTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DealService dealService;

    private ObjectMapper objectMapper;
    private LoanStatementRequestDto loanStatementRequest;
    private LoanOfferDto loanOffer;
    private FinishRegistrationRequestDto finishRegistrationRequest;
    private List<LoanOfferDto> loanOffers;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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

        loanOffers = Arrays.asList(
                LoanOfferDto.builder()
                        .statementId(UUID.randomUUID())
                        .requestedAmount(BigDecimal.valueOf(300000))
                        .totalAmount(BigDecimal.valueOf(300000))
                        .term(12)
                        .monthlyPayment(BigDecimal.valueOf(26500.50))
                        .rate(BigDecimal.valueOf(15.0))
                        .isInsuranceEnabled(false)
                        .isSalaryClient(false)
                        .build(),
                loanOffer
        );

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
    }

    @Test
    void createStatementSuccess() throws Exception {
        when(dealService.createStatement(any(LoanStatementRequestDto.class)))
                .thenReturn(loanOffers);

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanStatementRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].requestedAmount").value(300000))
                .andExpect(jsonPath("$[0].rate").value(15.0))
                .andExpect(jsonPath("$[1].isInsuranceEnabled").value(true));

        verify(dealService, times(1)).createStatement(any(LoanStatementRequestDto.class));
    }

    @Test
    void createStatementWithInvalidData() throws Exception {
        LoanStatementRequestDto invalidRequest = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(1000))
                .term(1)
                .firstName("")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(dealService, never()).createStatement(any());
    }

    @Test
    void selectOfferSuccess() throws Exception {
        doNothing().when(dealService).selectOffer(any(LoanOfferDto.class));

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanOffer)))
                .andExpect(status().isOk());

        verify(dealService, times(1)).selectOffer(any(LoanOfferDto.class));
    }

    @Test
    void calculateCreditSuccess() throws Exception {
        UUID statementId = UUID.randomUUID();
        doNothing().when(dealService)
                .calculateCredit(any(UUID.class), any(FinishRegistrationRequestDto.class));

        mockMvc.perform(post("/deal/calculate/" + statementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finishRegistrationRequest)))
                .andExpect(status().isOk());

        verify(dealService, times(1)).calculateCredit(eq(statementId), any(FinishRegistrationRequestDto.class));
    }

    @Test
    void calculateCredit_WhenStatementNotFound_ShouldPropagateException() throws Exception {
        UUID statementId = UUID.randomUUID();
        doThrow(new NotFoundException("Statement not found with id: " + statementId))
                .when(dealService).calculateCredit(any(UUID.class), any(FinishRegistrationRequestDto.class));

        mockMvc.perform(post("/deal/calculate/" + statementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finishRegistrationRequest)))
                .andExpect(status().isNotFound());

        verify(dealService, times(1)).calculateCredit(eq(statementId), any());
    }
}