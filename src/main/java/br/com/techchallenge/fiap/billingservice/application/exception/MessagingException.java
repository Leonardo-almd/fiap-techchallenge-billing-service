package br.com.techchallenge.fiap.billingservice.application.exception;

/**
 * Exception thrown when there's an error in messaging operations (SQS).
 */
public class MessagingException extends RuntimeException {

    public MessagingException(String message) {
        super(message);
    }

    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
