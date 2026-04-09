package ru.gudoshnikova.statement.exception;

public class PrescoringFailedException extends RuntimeException {
    public PrescoringFailedException(String message) {
        super(message);
    }

    public PrescoringFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
