package com.iubh.quizbackend.api.dto.changeRequest;

import com.iubh.quizbackend.entity.change.VoteType;
import lombok.Data;

@Data
public class VoteDto {
    private VoteType voteType;
}