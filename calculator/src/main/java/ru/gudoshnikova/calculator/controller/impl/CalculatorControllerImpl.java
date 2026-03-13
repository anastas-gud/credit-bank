package ru.gudoshnikova.calculator.controller.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gudoshnikova.calculator.controller.CalculatorController;
import ru.gudoshnikova.calculator.dto.CreditDto;
import ru.gudoshnikova.calculator.dto.LoanOfferDto;
import ru.gudoshnikova.calculator.dto.LoanStatementRequestDto;
import ru.gudoshnikova.calculator.dto.ScoringDataDto;
import ru.gudoshnikova.calculator.service.CalculatorService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/calculator")
@AllArgsConstructor
public class CalculatorControllerImpl implements CalculatorController {

    private final CalculatorService calculatorService;

    @Override
    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDto>> calculateOffers(LoanStatementRequestDto request) {
        log.info("A request has been received to calculate loan offers");
        log.info("Full input data: {}", request);

        List<LoanOfferDto> offers = calculatorService.calculateLoanOffers(request);
        log.info("Successfully calculated {} offers", offers.size());
        log.info("Calculated offers: {}", offers);

        return ResponseEntity.ok(offers);
    }

    @Override
    @PostMapping("/calc")
    public ResponseEntity<CreditDto> calculateCredit(ScoringDataDto scoringData) {
        log.info("A request has been received for a full loan settlement");
        log.info("Full input data: {}", scoringData);

        CreditDto credit = calculatorService.calculateCredit(scoringData);
        log.info("The loan was successfully calculated: {}", credit);

        return ResponseEntity.ok(credit);
    }
}
