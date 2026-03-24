package ru.gudoshnikova.calculator.service;

import ru.gudoshnikova.calculator.api.dto.CreditDto;
import ru.gudoshnikova.calculator.api.dto.LoanOfferDto;
import ru.gudoshnikova.calculator.api.dto.LoanStatementRequestDto;
import ru.gudoshnikova.calculator.api.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculateLoanOffers(LoanStatementRequestDto request);

    CreditDto calculateCredit(ScoringDataDto scoringData);
}
