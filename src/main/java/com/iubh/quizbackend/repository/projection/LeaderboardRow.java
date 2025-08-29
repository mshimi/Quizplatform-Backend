package com.iubh.quizbackend.repository.projection;

import java.util.UUID;

public interface LeaderboardRow {
    UUID getUserId();
    String getFirstName();
    String getName();
    int getScore();
}