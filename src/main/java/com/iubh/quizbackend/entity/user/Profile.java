package com.iubh.quizbackend.entity.user;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Profile {

    private String firstName;

    // This field corresponds to the 'name' you requested, typically the last name.
    private String name;
}