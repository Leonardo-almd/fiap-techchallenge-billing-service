package br.com.techchallenge.fiap.billingservice.infrastructure.controller;

import br.com.techchallenge.fiap.billingservice.application.dto.ErrorMessageDto;
import br.com.techchallenge.fiap.billingservice.application.exception.InvalidDataException;
import br.com.techchallenge.fiap.billingservice.application.exception.MessagingException;
import br.com.techchallenge.fiap.billingservice.application.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessageDto> handleNotFoundException(
            NotFoundException ex, HttpServletRequest request) {
        log.error("Not found: {}", ex.getMessage());
        ErrorMessageDto error = ErrorMessageDto.of(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorMessageDto> handleInvalidDataException(
            InvalidDataException ex, HttpServletRequest request) {
        log.error("Invalid data: {}", ex.getMessage());
        ErrorMessageDto error = ErrorMessageDto.of(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageDto> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation error");
        
        ErrorMessageDto error = ErrorMessageDto.of(
            HttpStatus.BAD_REQUEST.value(),
            message,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorMessageDto> handleMessagingException(
            MessagingException ex, HttpServletRequest request) {
        log.error("Messaging error: {}", ex.getMessage(), ex);
        ErrorMessageDto error = ErrorMessageDto.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error processing message: " + ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageDto> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorMessageDto error = ErrorMessageDto.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
