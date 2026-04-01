package ru.gudoshnikova.deal.service;

import ru.gudoshnikova.deal.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.LoanStatementRequestDto;

import java.util.List;
import java.util.UUID;

public interface DealService {
    List<LoanOfferDto> createStatement(LoanStatementRequestDto request);

    void selectOffer(LoanOfferDto loanOfferDto);

    void calculateCredit(UUID statementId, FinishRegistrationRequestDto request);
}
