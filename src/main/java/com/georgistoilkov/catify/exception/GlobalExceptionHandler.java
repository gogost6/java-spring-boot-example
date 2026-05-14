package com.georgistoilkov.catify.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.georgistoilkov.catify.config.CorrelationIdFilter;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private Map<String, Object> errorBody(int status, String error, String message) {
                Map<String, Object> body = new HashMap<>();
                body.put("timestamp", Instant.now());
                body.put("status", status);
                body.put("error", error);
                body.put("message", message);
                String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
                if (correlationId != null) {
                        body.put("correlationId", correlationId);
                }
                return body;
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidation(
                        MethodArgumentNotValidException ex) {
                Map<String, Object> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
                String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
                if (correlationId != null) {
                        errors.put("correlationId", correlationId);
                }
                return ResponseEntity.badRequest().body(errors);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidJson() {
                return ResponseEntity.badRequest().body(errorBody(400, "Bad Request", "Invalid or missing JSON body"));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(errorBody(400, "Bad Request", ex.getMessage()));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<?> handleConflict(IllegalStateException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(409, "Conflict", ex.getMessage()));
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(404, "Not Found", ex.getMessage()));
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<?> handleForbidden(ForbiddenException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(403, "Forbidden", ex.getMessage()));
        }
}