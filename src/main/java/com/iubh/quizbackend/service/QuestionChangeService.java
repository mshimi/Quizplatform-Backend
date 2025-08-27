package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.changeRequest.CreateChangeRequestDto;
import com.iubh.quizbackend.api.dto.changeRequest.QuestionChangeRequestDto;
import com.iubh.quizbackend.api.dto.changeRequest.VoteDto;
import com.iubh.quizbackend.entity.change.*;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.ChoiceQuestionMapper;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import com.iubh.quizbackend.repository.QuestionChangeRequestRepository;
import com.iubh.quizbackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class QuestionChangeService {

    private final QuestionChangeRequestRepository changeRequestRepository;
    private final ChoiceQuestionRepository choiceQuestionRepository;
    private final UserRepository userRepository;
    private final ChoiceQuestionMapper choiceQuestionMapper;
    private static final int VOTE_THRESHOLD = 3;

    @Transactional(readOnly = true)
    public Page<QuestionChangeRequestDto> getChangeRequestsByModule(UUID moduleId, Pageable pageable) {
        Page<QuestionChangeRequest> requestsPage = changeRequestRepository.findByQuestion_ModuleId(moduleId, pageable);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return requestsPage.map(request -> convertToDto(request, currentUser));
    }

    @Transactional(readOnly = true)
    public Page<QuestionChangeRequestDto> getChangeRequestsByQuestion(UUID questionId, Pageable pageable) {
        Page<QuestionChangeRequest> requestsPage = changeRequestRepository.findByQuestionId(questionId, pageable);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return requestsPage.map(request -> convertToDto(request, currentUser));
    }

    @Transactional(readOnly = true)
    public Page<QuestionChangeRequestDto> getChangeRequestsForFollowedModules(Pageable pageable, ChangeRequestStatus status) {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (currentUser.getFollowedModules() == null || currentUser.getFollowedModules().isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> followedModuleIds = currentUser.getFollowedModules().stream()
                .map(Module::getId)
                .collect(Collectors.toList());

        Page<QuestionChangeRequest> requestsPage = changeRequestRepository.findByQuestion_ModuleIdInAndStatus(followedModuleIds, status, pageable);
        return requestsPage.map(request -> convertToDto(request, currentUser));
    }

    private QuestionChangeRequestDto convertToDto(QuestionChangeRequest entity, User currentUser) {
        QuestionChangeRequestDto dto;

        // Determine which DTO to instantiate based on the entity type
        if (entity instanceof IncorrectQuestionTextRequest r) {
            QuestionChangeRequestDto.IncorrectQuestionTextRequestDto specificDto = new QuestionChangeRequestDto.IncorrectQuestionTextRequestDto();
            specificDto.setProposedText(r.getProposedText());
            dto = specificDto;
        } else if (entity instanceof IncorrectAnswerRequest r) {
            QuestionChangeRequestDto.IncorrectAnswerRequestDto specificDto = new QuestionChangeRequestDto.IncorrectAnswerRequestDto();
            if (r.getTargetAnswer() != null) {
                specificDto.setTargetAnswerId(r.getTargetAnswer().getId());
                specificDto.setOldAnswerText(r.getTargetAnswer().getText());
                specificDto.setOldAnswerIsCorrect(r.getTargetAnswer().getIsCorrect());
            }
            specificDto.setProposedText(r.getProposedText());
            specificDto.setProposedIsCorrect(r.getProposedIsCorrect());
            dto = specificDto;
        } else if (entity instanceof SuggestDeletionRequest) {
            dto = new QuestionChangeRequestDto.SuggestDeletionRequestDto();
        } else if (entity instanceof DuplicateQuestionRequest r) {
            QuestionChangeRequestDto.DuplicateQuestionRequestDto specificDto = new QuestionChangeRequestDto.DuplicateQuestionRequestDto();
            // Fetch and map the duplicate question
            choiceQuestionRepository.findById(r.getDuplicateOfQuestionId())
                    .ifPresent(dupQuestion -> specificDto.setDuplicateOfQuestion(choiceQuestionMapper.toDto(dupQuestion)));
            dto = specificDto;
        } else {
            // This should not happen with the current structure
            throw new IllegalStateException("Unknown QuestionChangeRequest type: " + entity.getClass().getName());
        }

        // Populate common fields
        dto.setId(entity.getId());
        dto.setRequesterUsername(entity.getRequester().getUsername());
        dto.setStatus(entity.getStatus());
        dto.setJustification(entity.getJustification());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setQuestion(choiceQuestionMapper.toDto(entity.getQuestion()));
        dto.setRequestType(resolveRequestType(entity));

        long positiveVotes = entity.getVotes().stream().filter(v -> v.getVoteType() == VoteType.APPROVE).count();
        long negativeVotes = entity.getVotes().stream().filter(v -> v.getVoteType() == VoteType.REJECT).count();
        dto.setPositiveVotes(positiveVotes);
        dto.setNegativeVotes(negativeVotes);

        boolean hasVoted = entity.getVotes().stream().anyMatch(v -> v.getVoter().getId().equals(currentUser.getId()));
        dto.setCurrentUserHasVoted(hasVoted);

        return dto;
    }

    private ChangeRequestType resolveRequestType(QuestionChangeRequest entity) {
        if (entity instanceof IncorrectQuestionTextRequest) return ChangeRequestType.INCORRECT_QUESTION_TEXT;
        if (entity instanceof IncorrectAnswerRequest) return ChangeRequestType.INCORRECT_ANSWER;
        if (entity instanceof SuggestDeletionRequest) return ChangeRequestType.SUGGEST_DELETION;
        if (entity instanceof DuplicateQuestionRequest) return ChangeRequestType.DUPLICATE_QUESTION;
        return null; // Should not happen
    }

    // --- MUTATION METHODS (UNCHANGED) ---

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
        if (changeRequest instanceof IncorrectQuestionTextRequest textRequest) {
            ChoiceQuestion newQuestion = new ChoiceQuestion();
            newQuestion.setModule(originalQuestion.getModule());
            newQuestion.setQuestionText(textRequest.getProposedText());
            newQuestion.setActive(true);
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
            newQuestion.setQuestionText(originalQuestion.getQuestionText());
            newQuestion.setActive(true);
            Set<Answer> newAnswers = originalQuestion.getAnswers().stream().map(originalAnswer -> {
                Answer newAnswer = new Answer();
                if (originalAnswer.equals(answerRequest.getTargetAnswer())) {
                    newAnswer.setText(answerRequest.getProposedText());
                    newAnswer.setIsCorrect(answerRequest.getProposedIsCorrect());
                } else {
                    newAnswer.setText(originalAnswer.getText());
                    newAnswer.setIsCorrect(originalAnswer.getIsCorrect());
                }
                newAnswer.setQuestion(newQuestion);
                return newAnswer;
            }).collect(Collectors.toSet());
            newQuestion.setAnswers(newAnswers);
            choiceQuestionRepository.save(newQuestion);
        }
        changeRequest.setStatus(ChangeRequestStatus.APPROVED);
        changeRequest.setResolvedAt(LocalDateTime.now());
        changeRequestRepository.save(changeRequest);
    }
}
