package com.iubh.quizbackend.api.validation;



import com.iubh.quizbackend.api.dto.AnswerRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class AtLeastOneAnswerIsCorrectValidator implements ConstraintValidator<AtLeastOneAnswerIsCorrect, List<AnswerRequestDto>> {

    @Override
    public boolean isValid(List<AnswerRequestDto> answers, ConstraintValidatorContext context) {
        // Wenn die Liste null oder leer ist, ist das ein Fall für @NotEmpty, nicht für diese Logik.
        if (answers == null || answers.isEmpty()) {
            return true;
        }

        // Überprüft, ob mindestens ein Element in der Liste isCorrect = true hat.
        return answers.stream().anyMatch(AnswerRequestDto::isCorrect);
    }
}