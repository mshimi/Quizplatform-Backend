package com.iubh.quizbackend.api.dto;



import com.iubh.quizbackend.entity.question.ChoiceQuestion;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnswerDto {

    private UUID id;

    private String text;

    private Boolean isCorrect;

    private UUID questionId;
}
