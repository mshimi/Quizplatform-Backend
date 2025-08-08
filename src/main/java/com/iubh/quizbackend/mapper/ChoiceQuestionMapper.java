package com.iubh.quizbackend.mapper;

import com.iubh.quizbackend.api.dto.ChoiceQuestionDto;
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

   // @Mapping(source = "module.id", target = "moduleId")
    ChoiceQuestionDto toDto(ChoiceQuestion choiceQuestion);
}