package ru.gudoshnikova.statement.integration.deal.service;

import ru.gudoshnikova.statement.dto.LoanOfferDto;
import ru.gudoshnikova.statement.dto.LoanStatementRequestDto;

import java.util.List;

public interface DealService {
    List<LoanOfferDto> sendStatementRequest(LoanStatementRequestDto request);

    void sendSelectOfferRequest(LoanOfferDto loanOffer);
}
