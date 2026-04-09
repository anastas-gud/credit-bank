package ru.gudoshnikova.statement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gudoshnikova.statement.dto.LoanStatementRequestDto;
import ru.gudoshnikova.statement.exception.PrescoringFailedException;
import ru.gudoshnikova.statement.service.PrescoringService;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
public class PrescoringServiceImpl implements PrescoringService {
    @Override
    public void prescoring(LoanStatementRequestDto request) {
        log.info("The beginning of prescoring, data: {}", request);

        log.debug("The beginning of age verification");
        int age = Period.between(request.getBirthdate(), LocalDate.now()).getYears();
        if (age < 18) {
            log.warn("Prescoring failed: age less than 18");
            throw new PrescoringFailedException("The age must be at least 18 years old");
        }
        log.debug("Age has been successfully verified");
    }
}
