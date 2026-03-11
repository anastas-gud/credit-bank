package ru.gudoshnikova.calculator.service;

import ru.gudoshnikova.calculator.dto.ScoringDataDto;

import java.math.BigDecimal;

public interface ScoringService {
    BigDecimal score(ScoringDataDto scoringData);
}
