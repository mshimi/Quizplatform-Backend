package com.iubh.quizbackend.mapper;

import com.iubh.quizbackend.api.dto.ModuleDetailDto;
import com.iubh.quizbackend.api.dto.ModuleDto;
import com.iubh.quizbackend.api.dto.ModuleListItemDto;
import com.iubh.quizbackend.api.dto.ModuleSummaryDto;
import com.iubh.quizbackend.entity.module.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { ChoiceQuestionMapper.class } // This mapper will use the question mapper
)
public interface ModuleMapper {

    ModuleMapper INSTANCE = Mappers.getMapper(ModuleMapper.class);

    // Map the entity field 'numberOfChoiceQuestions' to the DTO field 'numberOfQuestions'
    @Mapping(source = "numberOfChoiceQuestions", target = "numberOfQuestions")
    // Map the entity field 'questions' to the DTO field 'questions' using the ChoiceQuestionMapper
    @Mapping(source = "questions", target = "questions")
    ModuleDto toDto(Module module);

    List<ModuleDto> toDtoList(List<Module> modules);


    ModuleSummaryDto toSummaryDto(Module module);



    @Mapping(source = "numberOfChoiceQuestions", target = "numberOfQuestions")
    @Mapping(source = "likeCount", target = "likeCount") // Add this line
    @Mapping(target = "isFollowed", ignore = true)
    ModuleListItemDto toListItemDto(Module module);



}