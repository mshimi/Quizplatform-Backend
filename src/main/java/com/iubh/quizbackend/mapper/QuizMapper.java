package com.iubh.quizbackend.mapper;

import com.iubh.quizbackend.api.dto.QuizSummaryDto;
import com.iubh.quizbackend.entity.quiz.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for the Quiz entity and its DTOs.
 */
@Mapper(
        componentModel = "spring",
        // This tells MapStruct it can use other mappers, like ModuleMapper, when needed.
        uses = { ModuleMapper.class }
)
public interface QuizMapper {

    QuizMapper INSTANCE = Mappers.getMapper(QuizMapper.class);

    /**
     * Maps a Quiz entity to a QuizSummaryDto.
     * MapStruct automatically uses the getters on the Quiz entity (e.g., getNumberOfQuestions())
     * and the toSummaryDto method from ModuleMapper for the 'module' field.
     */
    QuizSummaryDto toSummaryDto(Quiz quiz);
}