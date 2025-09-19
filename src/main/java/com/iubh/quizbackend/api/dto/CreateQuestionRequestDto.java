package com.iubh.quizbackend.api.dto;

import com.iubh.quizbackend.api.validation.AtLeastOneAnswerIsCorrect;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import java.util.List;

// This represents the entire form payload
@Data
@Builder
public class CreateQuestionRequestDto {

    @NotBlank(message = "Der Fragentext darf nicht leer sein.")
    @Size(min = 10, max = 1000, message = "Der Fragentext muss zwischen 10 und 1000 Zeichen lang sein.")

    private String questionText;

    @Valid // Validiert weiterhin jedes Objekt in der Liste
    @NotEmpty(message = "Eine Frage muss mindestens eine Antwort haben.")
    @Size(min = 2, message = "Eine Frage muss mindestens zwei Antwortmöglichkeiten haben.")
    @AtLeastOneAnswerIsCorrect
    private List<AnswerRequestDto> answers;
}