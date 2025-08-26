package com.iubh.quizbackend.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StatisticsDto {
    private OverallStatsDto overall;
    private List<ModuleStatDto> byModule;
    private List<ActivityDataPointDto> activity;
}
