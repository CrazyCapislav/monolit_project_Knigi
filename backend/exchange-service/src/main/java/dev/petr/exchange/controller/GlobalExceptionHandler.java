package dev.petr.exchange.controller;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    /**
     * Handle Feign client errors (4xx from other services)
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(
            FeignException ex,
            HttpServletRequest request
    ) {
        log.error("Feign client error: {} - {}", ex.status(), ex.getMessage());

        HttpStatus status;
        String message;

        if (ex instanceof FeignException.BadRequest) {
            status = HttpStatus.BAD_REQUEST;
            message = extractFeignMessage(ex, "Invalid request to external service");
        } else if (ex instanceof FeignException.NotFound) {
            status = HttpStatus.NOT_FOUND;
            message = extractFeignMessage(ex, "Resource not found");
        } else if (ex instanceof FeignException.Forbidden) {
            status = HttpStatus.FORBIDDEN;
            message = extractFeignMessage(ex, "Access denied");
        } else if (ex instanceof FeignException.Unauthorized) {
            status = HttpStatus.UNAUTHORIZED;
            message = extractFeignMessage(ex, "Unauthorized");
        } else if (ex.status() >= 400 && ex.status() < 500) {
            status = HttpStatus.valueOf(ex.status());
            message = extractFeignMessage(ex, "Client error");
        } else {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "External service is temporarily unavailable";
        }

        return buildErrorResponse(status, message, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                request.getRequestURI()
        );
    }

    private String extractFeignMessage(FeignException ex, String defaultMessage) {
        String content = ex.contentUTF8();
        if (content != null && !content.isEmpty()) {
            try {
                int msgStart = content.indexOf("\"message\":\"");
                if (msgStart != -1) {
                    int valueStart = msgStart + 11;
                    int valueEnd = content.indexOf("\"", valueStart);
                    if (valueEnd != -1) {
                        return content.substring(valueStart, valueEnd);
                    }
                }
            } catch (Exception e) {
                log.debug("Could not parse Feign error message", e);
            }
        }
        return defaultMessage;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            String path
    ) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", OffsetDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", path
        ));
    }
}