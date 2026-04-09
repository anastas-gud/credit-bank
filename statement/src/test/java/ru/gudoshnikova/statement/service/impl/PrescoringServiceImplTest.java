package ru.gudoshnikova.statement.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gudoshnikova.statement.dto.LoanStatementRequestDto;
import ru.gudoshnikova.statement.exception.PrescoringFailedException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PrescoringServiceImplTest {

    @InjectMocks
    private PrescoringServiceImpl prescoringService;

    private LoanStatementRequestDto loanStatementRequestDto;

    @BeforeEach
    void setUp() {
        loanStatementRequestDto = LoanStatementRequestDto.builder()
                .amount(BigDecimal.valueOf(300000))
                .term(24)
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .email("ivan@mail.ru")
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("3452")
                .passportNumber("523698")
                .build();
    }

    @Test
    @DisplayName("Успешный прескоринг")
    void prescoringSuccess() {
        assertDoesNotThrow(() -> prescoringService.prescoring(loanStatementRequestDto));
    }

    @Test
    @DisplayName("Прескоринг завершен с ошибкой при проверке возраста")
    void prescoringFailedException() {
        loanStatementRequestDto.setBirthdate(LocalDate.now().minusYears(17));
        PrescoringFailedException ex = assertThrows(PrescoringFailedException.class,
                () -> prescoringService.prescoring(loanStatementRequestDto));
        assertEquals("The age must be at least 18 years old", ex.getMessage());
    }
}