package com.iubh.quizbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.UUID;

/**
 * A lightweight DTO for representing a module in a list or paginated view.
 * It includes a flag to indicate if the current user is following the module.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleListItemDto {
    private UUID id;
    private String title;
    private String description;
    private int numberOfQuestions;
    private int likeCount;
    private Boolean isFollowed;
}