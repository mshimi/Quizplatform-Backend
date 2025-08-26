package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.ActivityDataPointDto;
import com.iubh.quizbackend.api.dto.ModuleStatDto;
import com.iubh.quizbackend.api.dto.OverallStatsDto;
import com.iubh.quizbackend.api.dto.StatisticsDto;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final QuizRepository quizRepository;

    public StatisticsDto getStatisticsForUser(User currentUser, String timeframe) {
        LocalDateTime startDate = calculateStartDate(timeframe);
        List<Object[]> rawData = quizRepository.findStatisticsByUserIdAndDate(currentUser.getId(), startDate);

        // --- Module and Overall stats logic (remains the same) ---
        Map<UUID, List<Object[]>> groupedByModule = rawData.stream()
                .collect(Collectors.groupingBy(row -> (UUID) row[0]));

        List<ModuleStatDto> moduleStats = groupedByModule.entrySet().stream().map(entry -> {
            UUID moduleId = entry.getKey();
            String moduleTitle = (String) entry.getValue().get(0)[1];
            int quizzesPlayed = entry.getValue().size();
            long totalCorrect = entry.getValue().stream().mapToLong(row -> (long) row[3]).sum();
            long totalAnswered = entry.getValue().stream().mapToLong(row -> ((Number) row[4]).longValue()).sum();
            double avgScore = entry.getValue().stream()
                    .mapToDouble(row -> ((long) row[3] * 100.0) / ((Number) row[4]).intValue())
                    .average().orElse(0.0);

            return ModuleStatDto.builder()
                    .moduleId(moduleId).moduleTitle(moduleTitle).quizzesPlayed(quizzesPlayed)
                    .correctAnswers((int) totalCorrect).totalAnswers((int) totalAnswered)
                    .averageScore((int) Math.round(avgScore)).build();
        }).collect(Collectors.toList());

        int totalQuizzes = rawData.size();
        long totalCorrectOverall = moduleStats.stream().mapToLong(ModuleStatDto::getCorrectAnswers).sum();
        long totalAnsweredOverall = moduleStats.stream().mapToLong(ModuleStatDto::getTotalAnswers).sum();
        double avgScoreOverall = moduleStats.stream().mapToDouble(ModuleStatDto::getAverageScore).average().orElse(0.0);

        OverallStatsDto overallStats = OverallStatsDto.builder()
                .quizzesCompleted(totalQuizzes)
                .totalQuestionsAnswered((int) totalAnsweredOverall)
                .correctAnswerRatio(totalAnsweredOverall > 0 ? (int) Math.round((double) totalCorrectOverall * 100 / totalAnsweredOverall) : 0)
                .averageScore((int) Math.round(avgScoreOverall)).build();

        // --- NEW: Activity Chart Data Calculation ---
        List<ActivityDataPointDto> activityData = calculateActivity(rawData, timeframe);

        return StatisticsDto.builder()
                .overall(overallStats)
                .byModule(moduleStats)
                .activity(activityData)
                .build();
    }

    // --- (calculateStartDate remains the same) ---
    private LocalDateTime calculateStartDate(String timeframe) {
        LocalDate now = LocalDate.now();
        switch (timeframe) {
            case "Heute": return now.atStartOfDay();
            case "Letzte Woche": return now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
            case "Letzter Monat": return now.minusMonths(1).withDayOfMonth(1).atStartOfDay();
            default: return LocalDateTime.of(1970, 1, 1, 0, 0); // "Gesamt"
        }
    }

    // --- REWRITTEN: calculateActivity with full logic ---
    private List<ActivityDataPointDto> calculateActivity(List<Object[]> rawData, String timeframe) {
        if (rawData.isEmpty()) {
            return Collections.emptyList();
        }

        // Define how to group the data based on the timeframe
        Function<LocalDateTime, String> grouper;
        Comparator<ActivityDataPointDto> sorter;

        switch (timeframe) {
            case "Letzte Woche":
                grouper = dt -> dt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.GERMAN);
                sorter = Comparator.comparing(p -> DayOfWeek.from(DateTimeFormatter.ofPattern("E", Locale.GERMAN).parse(p.getDate())));
                break;
            case "Letzter Monat":
                WeekFields weekFields = WeekFields.of(Locale.GERMAN);
                grouper = dt -> "Woche " + dt.get(weekFields.weekOfMonth());
                sorter = Comparator.comparing(ActivityDataPointDto::getDate);
                break;
            default: // "Gesamt" or "Heute" (can be refined for hourly)
                grouper = dt -> dt.format(DateTimeFormatter.ofPattern("MMM", Locale.GERMAN));
                sorter = Comparator.comparing(p -> LocalDate.parse("01." + p.getDate() + ".2000", DateTimeFormatter.ofPattern("dd.MMM.yyyy", Locale.GERMAN)));
                break;
        }

        Map<String, List<Object[]>> groupedData = rawData.stream()
                .collect(Collectors.groupingBy(row -> grouper.apply((LocalDateTime) row[2])));

        return groupedData.entrySet().stream()
                .map(entry -> {
                    String dateLabel = entry.getKey();
                    long quizzes = entry.getValue().size();
                    double avgScore = entry.getValue().stream()
                            .mapToDouble(row -> ((long) row[3] * 100.0) / ((Number) row[4]).intValue())
                            .average().orElse(0.0);
                    return new ActivityDataPointDto(dateLabel, quizzes, Math.round(avgScore));
                })
                .sorted(sorter)
                .collect(Collectors.toList());
    }
}