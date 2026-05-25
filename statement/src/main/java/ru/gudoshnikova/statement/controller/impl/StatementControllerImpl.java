package ru.gudoshnikova.statement.controller.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.gudoshnikova.statement.controller.StatementController;
import ru.gudoshnikova.statement.dto.LoanOfferDto;
import ru.gudoshnikova.statement.dto.LoanStatementRequestDto;
import ru.gudoshnikova.statement.service.StatementService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
public class StatementControllerImpl implements StatementController {

    private final StatementService statementService;

    @Override
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto request) {
        log.info("POST /statement - Creating statement");
        log.debug("Request: {}", request);

        List<LoanOfferDto> offers = statementService.createStatement(request);

        log.info("Successfully created statement with {} offers", offers.size());
        return ResponseEntity.ok(offers);
    }

    @Override
    public ResponseEntity<Void> selectOffer(LoanOfferDto loanOffer) {
        log.info("POST /statement/offer - Selecting offer");
        log.debug("Offer: {}", loanOffer);

        statementService.selectOffer(loanOffer);

        log.info("Successfully selected offer for statement: {}", loanOffer.getStatementId());
        return ResponseEntity.ok().build();
    }
}
