package ru.gudoshnikova.statement.integration.deal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.gudoshnikova.statement.dto.LoanOfferDto;
import ru.gudoshnikova.statement.dto.LoanStatementRequestDto;
import ru.gudoshnikova.statement.integration.deal.service.DealService;
import ru.gudoshnikova.statement.util.ApiConstants;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final RestClient dealRestClient;

    @Override
    public List<LoanOfferDto> sendStatementRequest(LoanStatementRequestDto request) {
        log.debug("Sending request to Deal service at {}", ApiConstants.DEAL_STATEMENT);

        return dealRestClient
                .post()
                .uri(ApiConstants.DEAL_STATEMENT)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public void sendSelectOfferRequest(LoanOfferDto loanOffer) {
        log.debug("Sending request to Deal service at {}", ApiConstants.DEAL_OFFER_SELECT);

        dealRestClient.post()
                .uri(ApiConstants.DEAL_OFFER_SELECT)
                .body(loanOffer)
                .retrieve()
                .toBodilessEntity();
    }
}
