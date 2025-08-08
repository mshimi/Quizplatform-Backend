package com.iubh.quizbackend.api.dto;

import com.iubh.quizbackend.entity.question.ChoiceQuestionType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoiceQuestionDto {

    private UUID id;

    private String questionText;

    private ChoiceQuestionType questionType;

    private int correctAnswerCount;

    private int incorrectAnswerCount;

    private ModuleSummaryDto module;

   // private UUID moduleId;

    private Set<AnswerDto> answers = new HashSet<>();

    private ChangeRequestCountsDto changeRequestCounts;


}
