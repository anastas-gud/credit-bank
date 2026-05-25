package ru.gudoshnikova.deal.controller.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.gudoshnikova.deal.controller.DealController;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.LoanStatementRequestDto;
import ru.gudoshnikova.deal.dto.FinishRegistrationRequestDto;
import ru.gudoshnikova.deal.service.DealService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
public class DealControllerImpl implements DealController {

    private final DealService dealService;

    @Override
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto request) {
        log.info("Creating statement with request: {}", request);

        List<LoanOfferDto> offers = dealService.createStatement(request);
        return ResponseEntity.ok(offers);
    }

    @Override
    public ResponseEntity<Void> selectOffer(LoanOfferDto loanOffer) {
        log.info("Selecting offer: {}", loanOffer);

        dealService.selectOffer(loanOffer);

        log.info("Successfully selected offer for statement: {}", loanOffer.getStatementId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> calculateCredit(UUID statementId,
                                                FinishRegistrationRequestDto request) {
        log.info("Calculating credit for statement: {}, with request: {}", statementId, request);

        dealService.calculateCredit(statementId, request);

        log.info("Successfully calculated credit for statement: {}", statementId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> sendDocuments(UUID statementId) {
        log.info("POST /deal/document/{}/send - Sending documents", statementId);

        dealService.sendDocuments(statementId);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> signDocuments(UUID statementId) {
        log.info("POST /deal/document/{}/sign - Signing documents", statementId);

        dealService.signDocuments(statementId);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> verifyCode(UUID statementId, String code) {
        log.info("POST /deal/document/{}/code - Verifying code", statementId);

        dealService.verifyCode(statementId, code);

        return ResponseEntity.ok().build();
    }
}
