package com.iubh.quizbackend.api.dto.changeRequest;

import com.iubh.quizbackend.api.dto.AnswerDto;
import com.iubh.quizbackend.entity.change.ChangeRequestType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateChangeRequestDto {
    private ChangeRequestType type;
    private String justification;
    private String newQuestionText;
    private List<AnswerDto> newAnswers;
    private UUID duplicateQuestionId;
}
