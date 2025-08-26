package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.changeRequest.CreateChangeRequestDto;
import com.iubh.quizbackend.api.dto.changeRequest.VoteDto;
import com.iubh.quizbackend.entity.change.*;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import com.iubh.quizbackend.repository.QuestionChangeRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class QuestionChangeService {


    private final QuestionChangeRequestRepository changeRequestRepository;
    private final ChoiceQuestionRepository choiceQuestionRepository;
    private static final int VOTE_THRESHOLD = 3;


    public Page<QuestionChangeRequest> getChangeRequestsByModule(UUID moduleId, Pageable pageable) {
        return changeRequestRepository.findByQuestion_ModuleId(moduleId, pageable);
    }



    public Page<QuestionChangeRequest> getChangeRequestsByQuestion(UUID questionId, Pageable pageable) {
        return changeRequestRepository.findByQuestionId(questionId, pageable);
    }

    @Transactional
    public void addChangeRequest(UUID questionId, CreateChangeRequestDto requestDto) {
        ChoiceQuestion question = choiceQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + questionId));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        QuestionChangeRequest changeRequest;

        switch (requestDto.getType()) {
            case INCORRECT_QUESTION_TEXT:
                changeRequest = new IncorrectQuestionTextRequest();
                ((IncorrectQuestionTextRequest) changeRequest).setProposedText(requestDto.getNewQuestionText());
                break;
            case INCORRECT_ANSWER:
                changeRequest = new IncorrectAnswerRequest();
                // Logic to handle incorrect answer suggestions
                break;
            case SUGGEST_DELETION:
                changeRequest = new SuggestDeletionRequest();
                break;
            case DUPLICATE_QUESTION:
                changeRequest = new DuplicateQuestionRequest();
                ((DuplicateQuestionRequest) changeRequest).setDuplicateOfQuestionId(requestDto.getDuplicateQuestionId());
                break;
            default:
                throw new IllegalArgumentException("Unsupported change request type: " + requestDto.getType());
        }

        changeRequest.setQuestion(question);
        changeRequest.setRequester(currentUser);
        changeRequest.setJustification(requestDto.getJustification());
        changeRequest.setStatus(ChangeRequestStatus.PENDING);

        changeRequestRepository.save(changeRequest);
    }

    @Transactional
    public void voteForChangeRequest(UUID changeRequestId, VoteDto voteDto) {
        QuestionChangeRequest changeRequest = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Change request not found with id: " + changeRequestId));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (changeRequest.getVotes().stream().anyMatch(vote -> vote.getVoter().equals(currentUser))) {
            throw new IllegalStateException("User has already voted on this change request.");
        }

        ChangeRequestVote vote = new ChangeRequestVote();
        vote.setChangeRequest(changeRequest);
        vote.setVoter(currentUser);
        vote.setVoteType(voteDto.getVoteType());

        changeRequest.getVotes().add(vote);
        changeRequestRepository.save(changeRequest);

        commitChangeRequest(changeRequest);
    }


    private void commitChangeRequest(QuestionChangeRequest changeRequest) {
        // Only process pending requests
        if (changeRequest.getStatus() != ChangeRequestStatus.PENDING) {
            return;
        }

        long positiveVotes = changeRequest.getVotes().stream().filter(vote -> vote.getVoteType() == VoteType.APPROVE).count();
        long negativeVotes = changeRequest.getVotes().stream().filter(vote -> vote.getVoteType() == VoteType.REJECT).count();

        if (positiveVotes >= VOTE_THRESHOLD) {
            applyApprovedChange(changeRequest);
        } else if (negativeVotes >= VOTE_THRESHOLD) {
            changeRequest.setStatus(ChangeRequestStatus.REJECTED);
            changeRequest.setResolvedAt(LocalDateTime.now());
            changeRequestRepository.save(changeRequest);
        }
    }


    private void applyApprovedChange(QuestionChangeRequest changeRequest) {
        ChoiceQuestion originalQuestion = changeRequest.getQuestion();
        originalQuestion.setActive(false);
        choiceQuestionRepository.save(originalQuestion);

        // Handle logic based on the specific type of change request
        if (changeRequest instanceof IncorrectQuestionTextRequest textRequest) {
            ChoiceQuestion newQuestion = new ChoiceQuestion();
            newQuestion.setModule(originalQuestion.getModule());
            newQuestion.setQuestionText(textRequest.getProposedText()); // Use proposed text
            newQuestion.setActive(true);

            // Copy answers to the new question
            Set<Answer> newAnswers = originalQuestion.getAnswers().stream().map(answer -> {
                Answer newAnswer = new Answer();
                newAnswer.setText(answer.getText());
                newAnswer.setIsCorrect(answer.getIsCorrect());
                newAnswer.setQuestion(newQuestion);
                return newAnswer;
            }).collect(Collectors.toSet());
            newQuestion.setAnswers(newAnswers);
            choiceQuestionRepository.save(newQuestion);

        } else if (changeRequest instanceof IncorrectAnswerRequest answerRequest) {
            ChoiceQuestion newQuestion = new ChoiceQuestion();
            newQuestion.setModule(originalQuestion.getModule());
            newQuestion.setQuestionText(originalQuestion.getQuestionText()); // Keep original text
            newQuestion.setActive(true); // not needed just for sure

            // Create new answers, modifying the target answer
            Set<Answer> newAnswers = originalQuestion.getAnswers().stream().map(originalAnswer -> {
                Answer newAnswer = new Answer();
                if (originalAnswer.equals(answerRequest.getTargetAnswer())) {
                    // This is the answer to be changed
                    newAnswer.setText(answerRequest.getProposedText());
                    newAnswer.setIsCorrect(answerRequest.getProposedIsCorrect());
                } else {
                    // Copy other answers as they are
                    newAnswer.setText(originalAnswer.getText());
                    newAnswer.setIsCorrect(originalAnswer.getIsCorrect());
                }
                newAnswer.setQuestion(newQuestion);
                return newAnswer;
            }).collect(Collectors.toSet());
            newQuestion.setAnswers(newAnswers);
            choiceQuestionRepository.save(newQuestion);

        } else if (changeRequest instanceof SuggestDeletionRequest || changeRequest instanceof DuplicateQuestionRequest) {
            // For both deletion and duplicate suggestions, the action is the same:
            // The original question is already deactivated. No new question is created.
        }

        changeRequest.setStatus(ChangeRequestStatus.APPROVED);
        changeRequest.setResolvedAt(LocalDateTime.now());
        changeRequestRepository.save(changeRequest);
    }



    public Page<QuestionChangeRequest> getChangeRequestsForFollowedModules(Pageable pageable) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.getFollowedModules() == null || currentUser.getFollowedModules().isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> followedModuleIds = currentUser.getFollowedModules().stream()
                .map(Module::getId)
                .collect(Collectors.toList());

        return changeRequestRepository.findByQuestion_ModuleIdIn(followedModuleIds, pageable);
    }


}
