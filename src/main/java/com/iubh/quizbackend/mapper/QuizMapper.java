package com.iubh.quizbackend.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iubh.quizbackend.api.dto.*;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.quiz.Quiz;
import com.iubh.quizbackend.entity.quiz.QuizItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { ModuleMapper.class })
public abstract class QuizMapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModuleMapper moduleMapper;

    public abstract QuizSummaryDto toSummaryDto(Quiz quiz);

    @Mapping(target = "questions", source = "quizItems")
    @Mapping(target = "selectedAnswers", expression = "java(mapSelectedAnswers(quiz))") // Add this mapping
    public abstract QuizDetailDto toDetailDto(Quiz quiz);

    @Mapping(target = "questionText", source = "question.questionText")
    @Mapping(target = "id", source = "question.id")
    @Mapping(target = "answers", expression = "java(mapAndSortAnswers(quizItem))")
    protected abstract QuizQuestionDto quizItemToQuizQuestionDto(QuizItem quizItem);


    protected abstract QuizAnswerDto answerToQuizAnswerDto(Answer answer);

    protected List<QuizAnswerDto> mapAndSortAnswers(QuizItem quizItem) {
        try {
            List<UUID> shuffledIds = objectMapper.readValue(quizItem.getShuffledAnswerOrder(), new TypeReference<>() {});
            Map<UUID, Answer> originalAnswersMap = quizItem.getQuestion().getAnswers().stream()
                    .collect(Collectors.toMap(Answer::getId, answer -> answer));

            return shuffledIds.stream()
                    .map(originalAnswersMap::get)
                    .map(this::answerToQuizAnswerDto)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Could not deserialize shuffled answer order for quiz item: " + quizItem.getId());
            return Collections.emptyList();
        }
    }





    protected Map<UUID, UUID> mapSelectedAnswers(Quiz quiz) {
        return quiz.getQuizItems().stream()
                .filter(item -> item.getSelectedAnswer() != null)
                .collect(Collectors.toMap(
                        item -> item.getQuestion().getId(),
                        item -> item.getSelectedAnswer().getId()
                ));
    }


    public QuizResultDto toResultDto(Quiz quiz) {
        if (quiz == null) {
            return null;
        }

        long correctAnswersCount = quiz.getNumberOfCorrectAnswers();
        int totalQuestions = quiz.getNumberOfQuestions();
        int score = (totalQuestions > 0) ? (int) Math.round((double) correctAnswersCount * 100 / totalQuestions) : 0;

        List<QuestionResultDto> questionResults = quiz.getQuizItems().stream()
                .map(this::quizItemToQuestionResultDto)
                .collect(Collectors.toList());

        return QuizResultDto.builder()
                .id(quiz.getId())
                // 2. Use the injected moduleMapper to perform the conversion
                .module(moduleMapper.toSummaryDto(quiz.getModule()))
                .status(quiz.getStatus())
                .completedAt(quiz.getCompletedAt())
                .numberOfQuestions(totalQuestions)
                .numberOfCorrectAnswers(correctAnswersCount)
                .scorePercentage(score)
                .questionResults(questionResults)
                .build();
    }

    private QuestionResultDto quizItemToQuestionResultDto(QuizItem quizItem) {
        Answer selectedAnswer = quizItem.getSelectedAnswer();
        UUID selectedAnswerId = (selectedAnswer != null) ? selectedAnswer.getId() : null;

        List<AnswerResultDto> answerResults = quizItem.getQuestion().getAnswers().stream()
                .map(answer -> AnswerResultDto.builder()
                        .id(answer.getId())
                        .text(answer.getText())
                        .isCorrect(answer.getIsCorrect())
                        .isSelected(answer.getId().equals(selectedAnswerId))
                        .build())
                .collect(Collectors.toList());

        return QuestionResultDto.builder()
                .id(quizItem.getQuestion().getId())
                .questionText(quizItem.getQuestion().getQuestionText())
                .wasAnsweredCorrectly(Boolean.TRUE.equals(quizItem.getIsCorrect()))
                .answers(answerResults)
                .build();
    }


}