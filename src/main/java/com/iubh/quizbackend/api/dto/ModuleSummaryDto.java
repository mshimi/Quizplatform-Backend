package com.iubh.quizbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

/**
 * A simplified DTO for a Module, used to prevent circular dependencies when nested.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleSummaryDto {
    private UUID id;
    private String title;
}