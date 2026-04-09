package ru.gudoshnikova.statement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.gudoshnikova.statement.dto.LoanOfferDto;
import ru.gudoshnikova.statement.dto.LoanStatementRequestDto;
import ru.gudoshnikova.statement.service.PrescoringService;
import ru.gudoshnikova.statement.service.StatementService;
import ru.gudoshnikova.statement.util.ApiConstants;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final PrescoringService prescoringService;
    private final RestClient dealRestClient;

    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        log.info("Creating statement for request: {}", request);

        prescoringService.prescoring(request);
        log.info("Prescoring completed successfully");

        log.info("Sending request to Deal service at /deal/statement");
        List<LoanOfferDto> offers = dealRestClient
                .post()
                .uri(ApiConstants.DEAL_STATEMENT)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        log.info("Received {} offers from Deal service", offers.size());
        log.debug("Offers: {}", offers);

        return offers;
    }

    @Override
    public void selectOffer(LoanOfferDto loanOffer) {
        log.info("Selecting offer: {}", loanOffer);

        dealRestClient.post()
                .uri(ApiConstants.DEAL_OFFER_SELECT)
                .body(loanOffer)
                .retrieve()
                .toBodilessEntity();

        log.info("Offer selected successfully for statement: {}", loanOffer.getStatementId());
    }
}
