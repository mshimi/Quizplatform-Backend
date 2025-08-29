// src/main/java/com/iubh/quizbackend/api/error/ApiFieldError.java
package com.iubh.quizbackend.api.error;

public record ApiFieldError(
        String field,
        String message,
        String code,
        Object rejectedValue
) {}
