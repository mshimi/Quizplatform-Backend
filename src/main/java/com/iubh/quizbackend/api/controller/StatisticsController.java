package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.StatisticsDto;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<StatisticsDto> getUserStatistics(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "Gesamt") String timeframe
    ) {
        StatisticsDto stats = statisticsService.getStatisticsForUser(currentUser, timeframe);
        return ResponseEntity.ok(stats);
    }
}