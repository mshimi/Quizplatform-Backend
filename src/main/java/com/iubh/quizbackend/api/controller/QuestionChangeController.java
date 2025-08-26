package com.iubh.quizbackend.api.controller;


import com.iubh.quizbackend.api.dto.changeRequest.CreateChangeRequestDto;
import com.iubh.quizbackend.api.dto.changeRequest.QuestionChangeRequestDto;
import com.iubh.quizbackend.api.dto.changeRequest.VoteDto;
import com.iubh.quizbackend.entity.change.QuestionChangeRequest;
import com.iubh.quizbackend.mapper.QuestionChangeRequestMapper;
import com.iubh.quizbackend.service.QuestionChangeService;
import com.iubh.quizbackend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/change-requests")
@RequiredArgsConstructor
public class QuestionChangeController {

    private final QuestionChangeService questionChangeService;
    private final QuestionChangeRequestMapper mapper;

    /**
     * Gets a paginated list of change requests for a specific module.
     *
     * @param moduleId The UUID of the module.
     * @param pageable Pagination information.
     * @return A page of question change requests.
     */
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<Page<QuestionChangeRequestDto>> getChangeRequestsByModule(
            @PathVariable UUID moduleId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QuestionChangeRequest> requests = questionChangeService.getChangeRequestsByModule(moduleId, pageable);
        return ResponseEntity.ok(requests.map(mapper::toDto));
    }

    @GetMapping("/followed-modules")
    public ResponseEntity<Page<QuestionChangeRequestDto>> getChangeRequestsForFollowedModules(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QuestionChangeRequest> requests = questionChangeService.getChangeRequestsForFollowedModules(pageable);
        return ResponseEntity.ok(requests.map(mapper::toDto));
    }

    /**
     * Gets a paginated list of change requests for a specific question.
     *
     * @param questionId The UUID of the question.
     * @param pageable   Pagination information.
     * @return A page of question change requests.
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<Page<QuestionChangeRequestDto>> getChangeRequestsByQuestion(
            @PathVariable UUID questionId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QuestionChangeRequest> requests = questionChangeService.getChangeRequestsByQuestion(questionId, pageable);
        return ResponseEntity.ok(requests.map(mapper::toDto));
    }

    /**
     * Submits a new change request for a specific question.
     *
     * @param questionId The UUID of the question to which the change request applies.
     * @param requestDto The DTO containing the details of the change request.
     * @return A response entity indicating success.
     */
    @PostMapping("/question/{questionId}")
    public ResponseEntity<Void> addChangeRequest(
            @PathVariable UUID questionId,
            @RequestBody CreateChangeRequestDto requestDto) {
        questionChangeService.addChangeRequest(questionId, requestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * Submits a vote for a specific change request.
     *
     * @param changeRequestId The UUID of the change request to vote on.
     * @param voteDto         The DTO containing the vote type.
     * @return A response entity indicating success.
     */
    @PostMapping("/{changeRequestId}/vote")
    public ResponseEntity<Void> voteForChangeRequest(
            @PathVariable UUID changeRequestId,
            @RequestBody VoteDto voteDto) {
        questionChangeService.voteForChangeRequest(changeRequestId, voteDto);
        return ResponseEntity.ok().build();
    }
}
