package ru.gudoshnikova.deal.integration.calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.gudoshnikova.deal.dto.CreditDto;
import ru.gudoshnikova.deal.dto.LoanOfferDto;
import ru.gudoshnikova.deal.dto.LoanStatementRequestDto;
import ru.gudoshnikova.deal.dto.ScoringDataDto;
import ru.gudoshnikova.deal.integration.calculator.service.CalculatorService;
import ru.gudoshnikova.deal.util.ApiConstants;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {
    private final RestClient calculatorRestClient;

    @Override
    public List<LoanOfferDto> sendOffersRequest(LoanStatementRequestDto request) {
        log.debug("Sending request to Calculator service at {}", ApiConstants.CALCULATOR_OFFERS);

        return calculatorRestClient
                .post()
                .uri(ApiConstants.CALCULATOR_OFFERS)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public CreditDto sendCalculateRequest(ScoringDataDto scoringDataDto) {
        log.debug("Sending request to Calculator service at {}", ApiConstants.CALCULATOR_CALC);

        return calculatorRestClient
                .post()
                .uri(ApiConstants.CALCULATOR_CALC)
                .body(scoringDataDto)
                .retrieve()
                .body(CreditDto.class);
    }
}
