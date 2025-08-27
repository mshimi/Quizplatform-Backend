package com.iubh.quizbackend.mapper;

import com.iubh.quizbackend.api.dto.changeRequest.ProposedAnswerDto;
import com.iubh.quizbackend.entity.change.ProposedAnswer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProposedAnswerMapper {
    ProposedAnswerDto toDto(ProposedAnswer proposedAnswer);
}