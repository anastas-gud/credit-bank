package ru.gudoshnikova.deal.integration.calculator.service;

import ru.gudoshnikova.deal.dto.CreditDto;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.LoanStatementRequestDto;
import ru.gudoshnikova.deal.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> sendOffersRequest(LoanStatementRequestDto request);

    CreditDto sendCalculateRequest(ScoringDataDto scoringDataDto);
}
