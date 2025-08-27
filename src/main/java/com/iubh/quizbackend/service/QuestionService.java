package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.CreateQuestionRequestDto;
import com.iubh.quizbackend.api.dto.question.QuestionSummaryDto;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.mapper.ChoiceQuestionMapper;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import com.iubh.quizbackend.repository.ModuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final ChoiceQuestionRepository choiceQuestionRepository;
    private final ModuleRepository moduleRepository;
    private final ChoiceQuestionMapper choiceQuestionMapper; // Add this


    /**
     * Creates a new ChoiceQuestion with its associated Answers and links it to a Module.
     * This operation is transactional, ensuring all data is saved together or not at all.
     *
     * @param moduleId The ID of the module this question belongs to.
     * @param requestDto The DTO containing the question text and answers.
     * @return The newly created and persisted ChoiceQuestion entity.
     */
    @Transactional
    public ChoiceQuestion createQuestion(UUID moduleId, CreateQuestionRequestDto requestDto) {
        // 1. Find the parent module. Throws an exception if not found.
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + moduleId));

        // 2. Create the new question entity from the DTO.
        ChoiceQuestion newQuestion = ChoiceQuestion.builder()
                .questionText(requestDto.getQuestionText())
                .module(module)
                .build();

        // 3. Create and associate each answer with the new question.
        requestDto.getAnswers().forEach(answerDto -> {
            Answer newAnswer = Answer.builder()
                    .text(answerDto.getText())
                    .isCorrect(answerDto.isCorrect())
                    .build();

            log.info(newAnswer.getText());
            log.info(String.valueOf(newAnswer.getIsCorrect()));
            log.info("New answer is {}", newAnswer);

            // The addAnswer helper method correctly sets the bidirectional relationship.
            newQuestion.addAnswer(newAnswer);
        });

        // 4. Save the new question. Due to CascadeType.ALL, the answers are saved automatically.
        return choiceQuestionRepository.save(newQuestion);
    }



    @Transactional(readOnly = true)
    public Page<QuestionSummaryDto> searchQuestions(UUID moduleId, String searchTerm, Pageable pageable) {
        Page<ChoiceQuestion> questionsPage;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            questionsPage = choiceQuestionRepository.findByModule_IdAndActiveTrue(moduleId, pageable);
        } else {
            questionsPage = choiceQuestionRepository.findByModule_IdAndQuestionTextContainingIgnoreCaseAndActiveTrue(moduleId, searchTerm, pageable);
        }
        return questionsPage.map(choiceQuestionMapper::toSummaryDto);
    }

}