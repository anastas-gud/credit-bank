package ru.gudoshnikova.statement.exception;

public class DealServiceException extends RuntimeException {
    public DealServiceException(String message) {
        super(message);
    }

    public DealServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
