package ru.gudoshnikova.calculator.service;

import ru.gudoshnikova.calculator.dto.CreditDto;
import ru.gudoshnikova.calculator.dto.LoanOfferDto;
import ru.gudoshnikova.calculator.dto.LoanStatementRequestDto;
import ru.gudoshnikova.calculator.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculateLoanOffers(LoanStatementRequestDto request);

    CreditDto calculateCredit(ScoringDataDto scoringData);
}
