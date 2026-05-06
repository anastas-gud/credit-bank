package ru.gudoshnikova.gateway.exception;

public class ClientHttpException extends RuntimeException {
    public ClientHttpException(String message) {
        super(message);
    }

    public ClientHttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
