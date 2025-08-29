// src/main/java/com/iubh/quizbackend/api/error/ApiError.java
package com.iubh.quizbackend.api.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,        // z.B. "Bad Request"
        String code,         // kurze, maschinenlesbare Fehlerkennung
        String message,      // human readable
        String path,         // request URI
        List<ApiFieldError> fieldErrors // optional für Validierung
) {}
