package com.iubh.quizbackend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivityDataPointDto {
    private String date; // e.g., "Mo", "Woche 33", "Aug '25"
    private long quizzes;
    private double avgScore;
}
