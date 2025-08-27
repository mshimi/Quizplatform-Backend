package com.iubh.quizbackend.mapper;

import com.iubh.quizbackend.api.dto.ChoiceQuestionDto;
import com.iubh.quizbackend.api.dto.question.QuestionSummaryDto;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(
        componentModel = "spring",
        uses = { AnswerMapper.class } // Tells MapStruct to use the AnswerMapper for the 'answers' field
)
public interface ChoiceQuestionMapper {

    ChoiceQuestionMapper INSTANCE = Mappers.getMapper(ChoiceQuestionMapper.class);

    // --- UPDATED MAPPINGS ---
    // These mappings take the flat @Formula fields from the entity
    // and map them to the nested DTO object.
    @Mapping(source = "totalChangeRequests", target = "changeRequestCounts.total")
    @Mapping(source = "questionTextChangeRequests", target = "changeRequestCounts.questionTextChange")
    @Mapping(source = "answerChangeRequests", target = "changeRequestCounts.answerChange")
    @Mapping(source = "duplicationChangeRequests", target = "changeRequestCounts.duplicationChange")
    @Mapping(source = "deletionRequests", target = "changeRequestCounts.deletionRequest")
    ChoiceQuestionDto toDto(ChoiceQuestion choiceQuestion);


    QuestionSummaryDto toSummaryDto(ChoiceQuestion choiceQuestion);

}