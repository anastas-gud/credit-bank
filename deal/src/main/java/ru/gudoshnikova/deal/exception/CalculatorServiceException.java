package ru.gudoshnikova.deal.exception;

import lombok.Getter;

@Getter
public class CalculatorServiceException extends RuntimeException {
    private final String errorType;

    public CalculatorServiceException(String message) {
        super(message);
        this.errorType = "UNKNOWN";
    }

    public CalculatorServiceException(String message, String errorType) {
        super(message);
        this.errorType = errorType;
    }

    public CalculatorServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = "UNKNOWN";
    }
}
