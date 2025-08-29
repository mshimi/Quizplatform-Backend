// src/main/java/com/iubh/quizbackend/api/error/GlobalExceptionHandler.java
package com.iubh.quizbackend.api.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- Helpers ----
    private ApiError build(HttpStatus status, String code, String message, String path) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                null
        );
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String code, String message, String path) {
        return ResponseEntity.status(status).body(build(status, code, message, path));
    }

    // ---- 400: Bad Request / Validierung ----

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest req) {
        List<ApiFieldError> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ApiFieldError(
                        fe.getField(),
                        // bevorzugt "defaultMessage", sonst codes
                        Optional.ofNullable(fe.getDefaultMessage()).orElse(fe.getCode()),
                        fe.getCode(),
                        fe.getRejectedValue()
                ))
                .toList();

        ApiError body = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_FAILED",
                "Request validation failed",
                req.getRequestURI(),
                fields
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest req) {
        List<ApiFieldError> fields = ex.getConstraintViolations().stream()
                .map(v -> new ApiFieldError(
                        v.getPropertyPath().toString(),
                        v.getMessage(),
                        v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        v.getInvalidValue()
                ))
                .toList();

        ApiError body = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "CONSTRAINT_VIOLATION",
                "Request constraint violations",
                req.getRequestURI(),
                fields
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex,
                                                      HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Malformed JSON request body", req.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex,
                                                       HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                "Missing request parameter: " + ex.getParameterName(),
                req.getRequestURI());
    }

    // ---- 401/403: AuthZ ----

    @ExceptionHandler({ AccessDeniedException.class, SecurityException.class })
    public ResponseEntity<ApiError> handleAccessDenied(Exception ex, HttpServletRequest req) {
        return respond(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), req.getRequestURI());
    }

    // ---- 404 ----

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return respond(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        return respond(HttpStatus.NOT_FOUND, "NO_HANDLER",
                "No endpoint found for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                req.getRequestURI());
    }

    // ---- 405 ----

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest req) {
        String supported = Optional.ofNullable(ex.getSupportedHttpMethods())
                .map(s -> s.stream()
                        .map(org.springframework.http.HttpMethod::name) // ⬅️ wichtig: konkreter Enum-Typ
                        .collect(java.util.stream.Collectors.joining(", ")))
                .orElse("n/a");

        String msg = "Method " + ex.getMethod() + " not supported. Supported: " + supported;
        return respond(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", msg, req.getRequestURI());
    }

    // ---- 409/422: Business ----

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return respond(HttpStatus.CONFLICT, "ILLEGAL_STATE", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return respond(HttpStatus.UNPROCESSABLE_ENTITY, "ILLEGAL_ARGUMENT", ex.getMessage(), req.getRequestURI());
    }

    // ---- 500: Fallback ----

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        // In Produktion: loggen mit Korrelation/Trace-ID
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", req.getRequestURI());
    }
}
