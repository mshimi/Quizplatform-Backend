package com.iubh.quizbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleDetailDto {
    private UUID id;
    private String title;
    private String description;
    private int numberOfQuestions;
    private int likeCount;
    private Boolean isFollowed; // Provides context for the current user
    private Page<ChoiceQuestionDto> questions;
}

