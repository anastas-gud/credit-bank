package ru.gudoshnikova.deal.exception;

public class CalculatorServiceException extends RuntimeException {
    public CalculatorServiceException(String message) {
        super(message);
    }

    public CalculatorServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
