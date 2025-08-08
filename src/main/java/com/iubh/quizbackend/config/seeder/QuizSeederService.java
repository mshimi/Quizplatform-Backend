package com.iubh.quizbackend.config.seeder;

import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.quiz.Quiz;
import com.iubh.quizbackend.entity.quiz.QuizItem;
import com.iubh.quizbackend.entity.quiz.QuizStatus;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuizSeederService {

    private final QuizRepository quizRepository;

    public void seedQuizzes(User user, List<Module> modules) {
        if (modules.isEmpty()) return;

        List<Quiz> quizzesToSave = new ArrayList<>();

        // Create a completed quiz for the first module
        Module module1 = modules.get(0);
        if (!module1.getQuestions().isEmpty()) {
            Quiz completedQuiz = Quiz.builder()
                    .user(user).module(module1).status(QuizStatus.COMPLETED)
                    .completedAt(LocalDateTime.now().minusDays(1)).build();

            for (ChoiceQuestion question : module1.getQuestions()) {
                QuizItem item = QuizItem.builder().question(question)
                        .answeredAt(LocalDateTime.now().minusDays(1).plusMinutes(5)).build();
                Answer selectedAnswer = question.getAnswers().stream().findFirst().orElse(null);
                if (selectedAnswer != null) {
                    item.setSelectedAnswers(Set.of(selectedAnswer));
                    item.setIsCorrect(selectedAnswer.getIsCorrect());
                }
                completedQuiz.addQuizItem(item);
            }
            quizzesToSave.add(completedQuiz);
        }

        // Create an in-progress quiz for the second module
        Module module2 = modules.get(1);
        if (!module2.getQuestions().isEmpty()) {
            Quiz inProgressQuiz = Quiz.builder()
                    .user(user).module(module2).status(QuizStatus.IN_PROGRESS).build();
            module2.getQuestions().stream().findFirst().ifPresent(question -> {
                QuizItem item = QuizItem.builder().question(question).build();
                inProgressQuiz.addQuizItem(item);
            });
            quizzesToSave.add(inProgressQuiz);
        }
        quizRepository.saveAll(quizzesToSave);
    }
}