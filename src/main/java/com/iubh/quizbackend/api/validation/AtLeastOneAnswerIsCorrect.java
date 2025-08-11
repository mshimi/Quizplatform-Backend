
package com.iubh.quizbackend.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validierungsannotation, die sicherstellt, dass in einer Liste von
 * AnswerRequestDto mindestens ein Element als 'isCorrect = true' markiert ist.
 */
@Target({ElementType.FIELD}) // Diese Annotation kann nur auf Feldern angewendet werden
@Retention(RetentionPolicy.RUNTIME) // Wird zur Laufzeit ausgewertet
@Constraint(validatedBy = AtLeastOneAnswerIsCorrectValidator.class) // Verweist auf die Logik-Klasse
public @interface AtLeastOneAnswerIsCorrect {

    String message() default "Es muss mindestens eine Antwort als korrekt markiert sein.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}