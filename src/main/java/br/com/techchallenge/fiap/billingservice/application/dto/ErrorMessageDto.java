package br.com.techchallenge.fiap.billingservice.application.dto;

import java.time.LocalDateTime;

/**
 * DTO for error responses.
 */
public record ErrorMessageDto(
    int status,
    String message,
    String path,
    LocalDateTime timestamp
) {
    public static ErrorMessageDto of(int status, String message, String path) {
        return new ErrorMessageDto(status, message, path, LocalDateTime.now());
    }
}
