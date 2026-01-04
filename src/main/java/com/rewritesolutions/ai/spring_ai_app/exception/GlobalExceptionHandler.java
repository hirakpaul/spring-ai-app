package com.rewritesolutions.ai.spring_ai_app.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Spring AI Application.
 * This class provides centralized exception handling across all {@code @RequestMapping} methods
 * through {@code @ExceptionHandler} methods.
 *
 * <p>Uses {@code @RestControllerAdvice} to intercept exceptions thrown by controllers
 * and return consistent error responses to API clients.</p>
 *
 * @author Rewrite Solutions
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles {@link CustomerNotFoundException} thrown when a customer is not found in the system.
     * Returns a 404 NOT FOUND HTTP status with a structured error response.
     *
     * @param ex the {@link CustomerNotFoundException} containing details about the missing customer
     * @return a {@link ResponseEntity} containing the error details and HTTP 404 status
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        log.error("Customer not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link IllegalArgumentException} thrown when invalid arguments are provided.
     * Returns a 400 BAD REQUEST HTTP status with a structured error response.
     *
     * @param ex the {@link IllegalArgumentException} containing details about the invalid argument
     * @return a {@link ResponseEntity} containing the error details and HTTP 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link MethodArgumentNotValidException} thrown when request body validation fails.
     * Extracts all field validation errors and returns them in a structured format with a
     * 400 BAD REQUEST HTTP status.
     *
     * <p>This method processes Bean Validation (JSR-303) errors from {@code @Valid} annotations
     * and provides detailed field-level error messages to help clients correct their requests.</p>
     *
     * @param ex the {@link MethodArgumentNotValidException} containing validation error details
     * @return a {@link ResponseEntity} containing a map with timestamp, status, error type,
     *         and field-specific validation errors, along with HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles all uncaught exceptions that are not handled by more specific exception handlers.
     * This is a catch-all handler that ensures all exceptions result in a consistent error response
     * and returns a 500 INTERNAL SERVER ERROR HTTP status.
     *
     * <p>This method logs the full stack trace for debugging purposes while returning a generic
     * error message to the client to avoid exposing internal implementation details.</p>
     *
     * @param ex the {@link Exception} that was not caught by other handlers
     * @return a {@link ResponseEntity} containing a generic error message and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Internal data class representing a standardized error response structure.
     * This class is used to create consistent error responses across all exception handlers.
     *
     * <p>Uses Lombok annotations for automatic generation of getters, setters, toString,
     * equals, hashCode methods, and builder pattern implementation.</p>
     */
    @lombok.Data
    @lombok.Builder
    private static class ErrorResponse {
        /** The timestamp when the error occurred */
        private LocalDateTime timestamp;

        /** The HTTP status code (e.g., 404, 400, 500) */
        private int status;

        /** A brief description of the error type (e.g., "Not Found", "Bad Request") */
        private String error;

        /** A detailed message describing the specific error that occurred */
        private String message;
    }
}
