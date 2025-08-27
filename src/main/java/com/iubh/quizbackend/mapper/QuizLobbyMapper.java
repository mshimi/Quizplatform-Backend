package com.iubh.quizbackend.mapper;

import com.iubh.quizbackend.api.dto.quiz.QuizLobbyDto;
import com.iubh.quizbackend.entity.quiz.QuizLobby;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = { UserMapper.class, ModuleMapper.class }
)
public interface QuizLobbyMapper {
    QuizLobbyDto toDto(QuizLobby quizLobby);
}
