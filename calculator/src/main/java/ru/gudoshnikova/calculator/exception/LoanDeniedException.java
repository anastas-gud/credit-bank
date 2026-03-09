package ru.gudoshnikova.calculator.exception;

public class LoanDeniedException extends RuntimeException {
    public LoanDeniedException(String message) {
        super(message);
    }

    public LoanDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
