package com.iubh.quizbackend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeRequestCountsDto {

    private int total;

    private int questionTextChange;

    private int answerChange;

    private int duplicationChange;

    private int deletionRequest;
}
