package com.iubh.quizbackend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// This represents a single answer within the request

public class AnswerRequestDto {

    @NotBlank(message = "Der Antworttext darf nicht leer sein.")
    @Size(min = 1, max = 1000, message = "Der Antworttext muss zwischen 1 und 1000 Zeichen lang sein.")

    private String text;

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @NotNull(message = "Die Angabe, ob die Antwort korrekt ist, darf nicht fehlen.")

    private Boolean isCorrect;




}