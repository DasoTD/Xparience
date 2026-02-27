package com.xparience.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Validation failed", errors));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage();

        if (message != null && message.contains("Too many attempts")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiResponse<>(false, message, extractDynamicFields(message)));
        }

        if (message != null && message.contains("temporarily locked")) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(new ApiResponse<>(false, message, extractDynamicFields(message)));
        }

        if (message != null && message.contains("CAPTCHA is required")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, message, Map.of("captchaRequired", true)));
        }

        Map<String, Object> details = extractDynamicFields(message);
        if (details.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(message));
        }

        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, message, details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }

    private Map<String, Object> extractDynamicFields(String message) {
        Map<String, Object> details = new HashMap<>();
        if (message == null) {
            return details;
        }

        if (message.contains("attemptsRemaining=")) {
            String value = message.substring(message.indexOf("attemptsRemaining=") + "attemptsRemaining=".length()).trim();
            int endIdx = value.indexOf(' ');
            String number = endIdx >= 0 ? value.substring(0, endIdx) : value;
            try {
                details.put("attemptsRemaining", Integer.parseInt(number));
            } catch (NumberFormatException ignored) {
            }
        }

        if (message.contains("resendAvailableInSeconds=")) {
            String value = message.substring(message.indexOf("resendAvailableInSeconds=") + "resendAvailableInSeconds=".length()).trim();
            int endIdx = value.indexOf(' ');
            String number = endIdx >= 0 ? value.substring(0, endIdx) : value;
            try {
                details.put("resendAvailableInSeconds", Long.parseLong(number));
            } catch (NumberFormatException ignored) {
            }
        }

        if (message.startsWith("Too many attempts. Retry in ") || message.contains("temporarily locked")) {
            String digits = message.replaceAll("\\D+", "");
            if (!digits.isBlank()) {
                try {
                    details.put("retryAfterSeconds", Long.parseLong(digits));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return details;
    }
}