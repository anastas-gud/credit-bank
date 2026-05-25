package ru.gudoshnikova.gateway.service;

import ru.gudoshnikova.gateway.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.gateway.dto.LoanOfferDto;
import ru.gudoshnikova.gateway.dto.LoanStatementRequestDto;
import ru.gudoshnikova.gateway.dto.StatementDto;

import java.util.List;
import java.util.UUID;

public interface GatewayService {
    List<LoanOfferDto> createStatement(LoanStatementRequestDto request);

    void selectOffer(LoanOfferDto loanOffer);

    void calculateCredit(UUID statementId, FinishRegistrationRequestDto request);

    void sendDocuments(UUID statementId);

    void signDocuments(UUID statementId);

    void verifyCode(UUID statementId, String code);

    StatementDto getStatementById(UUID statementId);

    List<StatementDto> getAllStatements();
}
