package ru.gudoshnikova.deal.service;

import ru.gudoshnikova.deal.api.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.deal.api.dto.LoanOfferDto;
import ru.gudoshnikova.deal.api.dto.LoanStatementRequestDto;

import java.util.List;
import java.util.UUID;

public interface DealService {
    List<LoanOfferDto> createStatement(LoanStatementRequestDto request);

    void selectOffer(LoanOfferDto loanOfferDto);

    void calculateCredit(UUID statementId, FinishRegistrationRequestDto request);
}
