package ru.gudoshnikova.calculator.service;

import ru.gudoshnikova.calculator.dto.LoanStatementRequestDto;

public interface PrescoringService {
    void prescoring(LoanStatementRequestDto request);
}
