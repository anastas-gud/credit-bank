package ru.gudoshnikova.calculator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "calculator")
public class CalculatorConfig {
    private BigDecimal baseRate;
    private BigDecimal insuranceCostPercent;
}
