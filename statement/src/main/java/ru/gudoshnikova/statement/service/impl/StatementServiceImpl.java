package ru.gudoshnikova.statement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.statement.dto.LoanOfferDto;
import ru.gudoshnikova.statement.dto.LoanStatementRequestDto;
import ru.gudoshnikova.statement.integration.deal.service.DealService;
import ru.gudoshnikova.statement.service.PrescoringService;
import ru.gudoshnikova.statement.service.StatementService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final PrescoringService prescoringService;
    private final DealService dealService;

    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        log.info("Creating statement for request: {}", request);

        prescoringService.prescoring(request);
        log.info("Prescoring completed successfully");

        log.info("Sending request to Deal service");
        List<LoanOfferDto> offers = dealService.sendStatementRequest(request);

        log.info("Received {} offers from Deal service", offers.size());
        log.debug("Offers: {}", offers);

        return offers;
    }

    @Override
    public void selectOffer(LoanOfferDto loanOffer) {
        log.info("Selecting offer: {}", loanOffer);

        dealService.sendSelectOfferRequest(loanOffer);

        log.info("Offer selected successfully for statement: {}", loanOffer.getStatementId());
    }
}
