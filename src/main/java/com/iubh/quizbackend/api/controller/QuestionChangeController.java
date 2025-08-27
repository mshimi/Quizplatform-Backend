package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.changeRequest.CreateChangeRequestDto;
import com.iubh.quizbackend.api.dto.changeRequest.QuestionChangeRequestDto;
import com.iubh.quizbackend.api.dto.changeRequest.VoteDto;
import com.iubh.quizbackend.entity.change.ChangeRequestStatus;
import com.iubh.quizbackend.service.QuestionChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/change-requests") // Corrected the base path
@RequiredArgsConstructor
public class QuestionChangeController {

    private final QuestionChangeService questionChangeService;
    // The mapper is no longer needed here

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<Page<QuestionChangeRequestDto>> getChangeRequestsByModule(
            @PathVariable UUID moduleId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QuestionChangeRequestDto> requests = questionChangeService.getChangeRequestsByModule(moduleId, pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/followed-modules")
    public ResponseEntity<Page<QuestionChangeRequestDto>> getChangeRequestsForFollowedModules(
            @RequestParam(defaultValue = "APPROVED") ChangeRequestStatus status, // Added status parameter with default

            @PageableDefault(size = 20) Pageable pageable) {
        Page<QuestionChangeRequestDto> requests = questionChangeService.getChangeRequestsForFollowedModules(pageable,status);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<Page<QuestionChangeRequestDto>> getChangeRequestsByQuestion(
            @PathVariable UUID questionId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QuestionChangeRequestDto> requests = questionChangeService.getChangeRequestsByQuestion(questionId, pageable);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/question/{questionId}")
    public ResponseEntity<Void> addChangeRequest(
            @PathVariable UUID questionId,
            @RequestBody CreateChangeRequestDto requestDto) {
        questionChangeService.addChangeRequest(questionId, requestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{changeRequestId}/vote")
    public ResponseEntity<Void> voteForChangeRequest(
            @PathVariable UUID changeRequestId,
            @RequestBody VoteDto voteDto) {
        questionChangeService.voteForChangeRequest(changeRequestId, voteDto);
        return ResponseEntity.ok().build();
    }
}
