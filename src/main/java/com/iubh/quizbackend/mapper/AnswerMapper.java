package com.iubh.quizbackend.mapper;

import com.iubh.quizbackend.api.dto.AnswerDto;
import com.iubh.quizbackend.entity.question.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring") // Creates a Spring Bean
public interface AnswerMapper {

    AnswerMapper INSTANCE = Mappers.getMapper(AnswerMapper.class);

    @Mapping(source = "question.id", target = "questionId")
    AnswerDto toDto(Answer answer);
}