package com.iubh.quizbackend.mapper;

import com.iubh.quizbackend.api.dto.changeRequest.QuestionChangeRequestDto;
import com.iubh.quizbackend.entity.change.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface QuestionChangeRequestMapper {

    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "question.questionText", target = "questionText")
    @Mapping(source = "requester.username", target = "requesterUsername")
    @Mapping(source = "entity", target = "requestType", qualifiedByName = "entityToRequestType")
    QuestionChangeRequestDto toDto(QuestionChangeRequest entity);

    @Named("entityToRequestType")
    default ChangeRequestType entityToRequestType(QuestionChangeRequest entity) {
        if (entity instanceof IncorrectQuestionTextRequest) {
            return ChangeRequestType.INCORRECT_QUESTION_TEXT;
        }
        if (entity instanceof IncorrectAnswerRequest) {
            return ChangeRequestType.INCORRECT_ANSWER;
        }
        if (entity instanceof SuggestDeletionRequest) {
            return ChangeRequestType.SUGGEST_DELETION;
        }
        if (entity instanceof DuplicateQuestionRequest) {
            return ChangeRequestType.DUPLICATE_QUESTION;
        }
        return null;
    }
}