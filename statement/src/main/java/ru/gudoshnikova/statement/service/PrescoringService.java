package ru.gudoshnikova.statement.service;

import ru.gudoshnikova.statement.dto.LoanStatementRequestDto;

public interface PrescoringService {
    void prescoring(LoanStatementRequestDto request);
}
