package com.iubh.quizbackend.config.seeder;

import com.iubh.quizbackend.entity.change.*;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.QuestionChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeRequestSeederService {

    private final QuestionChangeRequestRepository requestRepository;

    public void seedChangeRequests(List<User> users, List<Module> modules) {
        // Use more robust checks
        if (users.size() < 3) return;
        List<ChoiceQuestion> allQuestions = modules.stream()
                .flatMap(module -> module.getQuestions().stream()).toList();
        if (allQuestions.size() < 5) return;

        User requester1 = users.get(1);
        User requester2 = users.get(2);
        List<QuestionChangeRequest> requestsToSave = new ArrayList<>();

        // 1. IncorrectQuestionTextRequest
        IncorrectQuestionTextRequest textRequest = new IncorrectQuestionTextRequest();
        textRequest.setQuestion(allQuestions.get(0));
        textRequest.setRequester(requester1);
        textRequest.setJustification("The question seems to have a typo.");
        textRequest.setProposedText("Was ist eine 'Variable' in der Programmiersprache Java?");
        requestsToSave.add(textRequest);

        // 2. IncorrectAnswerRequest
        // Check if the question and its answers exist before proceeding
        ChoiceQuestion questionForAnswerChange = allQuestions.get(1);
        if (questionForAnswerChange != null && !questionForAnswerChange.getAnswers().isEmpty()) {
            questionForAnswerChange.getAnswers().stream().findFirst().ifPresent(answer -> {
                IncorrectAnswerRequest answerRequest = new IncorrectAnswerRequest();
                answerRequest.setQuestion(questionForAnswerChange);
                answerRequest.setRequester(requester2);
                answerRequest.setJustification("This answer is misleading.");
                answerRequest.setTargetAnswer(answer);
                answerRequest.setProposedText("O(log n) - Logarithmische Zeit");
                answerRequest.setProposedIsCorrect(true);
                requestsToSave.add(answerRequest);
            });
        }


        // 3. SuggestDeletionRequest
        SuggestDeletionRequest deletionRequest = new SuggestDeletionRequest();
        deletionRequest.setQuestion(allQuestions.get(2));
        deletionRequest.setRequester(requester1);
        deletionRequest.setJustification("This question is too simple.");
        requestsToSave.add(deletionRequest);

        // 4. DuplicateQuestionRequest
        DuplicateQuestionRequest duplicateRequest = new DuplicateQuestionRequest();
        duplicateRequest.setQuestion(allQuestions.get(3));
        duplicateRequest.setRequester(requester2);
        duplicateRequest.setJustification("This is a duplicate of another question.");
        duplicateRequest.setDuplicateOfQuestionId(allQuestions.get(4).getId());
        requestsToSave.add(duplicateRequest);

        if (!requestsToSave.isEmpty()) {
            requestRepository.saveAll(requestsToSave);
        }
    }
}