package com.iubh.quizbackend.api.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequestDto {

    @NotBlank(message = "Der Vorname darf nicht leer sein.")
    @Size(min = 2, max = 50, message = "Der Vorname muss zwischen 2 und 50 Zeichen lang sein.")
    private String firstName;

    @NotBlank(message = "Der Nachname darf nicht leer sein.")
    @Size(min = 2, max = 50, message = "Der Nachname muss zwischen 2 und 50 Zeichen lang sein.")
    private String name; // This corresponds to 'lastName' or 'Nachname'
}
