package br.com.techchallenge.fiap.billingservice.application.exception;

/**
 * Exception thrown when invalid data is provided.
 * This is a domain exception that represents business rule violations.
 */
public class InvalidDataException extends RuntimeException {

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
