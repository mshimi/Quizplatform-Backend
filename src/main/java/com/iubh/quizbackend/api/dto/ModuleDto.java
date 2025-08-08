package com.iubh.quizbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleDto {
    private UUID id;
    private String title;
    private String description;
    private int numberOfQuestions;
    private int likeCount;
    private Set<ChoiceQuestionDto> questions;
    private Set<UserDto> followers;
}
