package com.iubh.quizbackend.config.seeder;

import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class QuestionSeederService {

    private final ChoiceQuestionRepository choiceQuestionRepository;

    public void seedQuestions(List<Module> modules) {
        if (modules.isEmpty()) return;

        Random random = new Random();
        List<ChoiceQuestion> questionsToSave = new ArrayList<>();
        List<QuestionTemplate> templates = getQuestionTemplates();

        for (QuestionTemplate template : templates) {
            Module randomModule = modules.get(random.nextInt(modules.size()));
            ChoiceQuestion question = ChoiceQuestion.builder()
                    .questionText(template.questionText())
                    .module(randomModule)
                    .active(true)

                    .build();

            for (AnswerTemplate answerTemplate : template.answers()) {
                Answer answer = Answer.builder()
                        .text(answerTemplate.text())
                        .isCorrect(answerTemplate.isCorrect())
                        .build();
                question.addAnswer(answer);
            }
            // Manually update the in-memory Module object's collection
            randomModule.getQuestions().add(question);
            questionsToSave.add(question);
        }
        choiceQuestionRepository.saveAll(questionsToSave);
    }

    private record QuestionTemplate(String questionText, List<AnswerTemplate> answers) {}
    private record AnswerTemplate(String text, boolean isCorrect) {}

    private List<QuestionTemplate> getQuestionTemplates() {
        // Using the same list of 15 questions from the original DataLoader
        return List.of(
                new QuestionTemplate("Was ist eine Variable in Java?", List.of(new AnswerTemplate("Ein benannter Speicherort für Daten.", true), new AnswerTemplate("Eine Methode ohne Rückgabewert.", false))),
                new QuestionTemplate("Welche Komplexität hat eine binäre Suche in einem sortierten Array?", List.of(new AnswerTemplate("O(log n)", true), new AnswerTemplate("O(n)", false))),
                new QuestionTemplate("Wofür steht SQL?", List.of(new AnswerTemplate("Structured Query Language", true), new AnswerTemplate("Simple Query Logic", false))),
                new QuestionTemplate("Was ist die Hauptaufgabe eines Betriebssystems?", List.of(new AnswerTemplate("Ressourcenverwaltung (CPU, Speicher)", true), new AnswerTemplate("Textverarbeitung", false))),
                new QuestionTemplate("Was beschreibt das Wasserfallmodell im Software Engineering?", List.of(new AnswerTemplate("Ein sequentieller, linearer Entwicklungsprozess.", true), new AnswerTemplate("Ein iterativer und inkrementeller Ansatz.", false))),
                new QuestionTemplate("Welcher Datentyp wird für Fließkommazahlen in Java verwendet?", List.of(new AnswerTemplate("double", true), new AnswerTemplate("int", false))),
                new QuestionTemplate("Was ist ein 'Stack' als Datenstruktur?", List.of(new AnswerTemplate("Last-In, First-Out (LIFO)", true), new AnswerTemplate("First-In, First-Out (FIFO)", false))),
                new QuestionTemplate("Welcher SQL-Befehl wird zum Lesen von Daten verwendet?", List.of(new AnswerTemplate("SELECT", true), new AnswerTemplate("INSERT", false))),
                new QuestionTemplate("Was ist ein 'Prozess' im Kontext von Betriebssystemen?", List.of(new AnswerTemplate("Ein Programm in Ausführung.", true), new AnswerTemplate("Eine Datei auf der Festplatte.", false))),
                new QuestionTemplate("Wofür steht 'Agile' im Software Engineering?", List.of(new AnswerTemplate("Eine Sammlung von iterativen Entwicklungsmethoden.", true), new AnswerTemplate("Ein spezifisches Programmierparadigma.", false))),
                new QuestionTemplate("Was ist Polymorphismus in der OOP?", List.of(new AnswerTemplate("Die Fähigkeit eines Objekts, viele Formen anzunehmen.", true), new AnswerTemplate("Das Verstecken von Implementierungsdetails.", false))),
                new QuestionTemplate("Was ist ein 'Hash-Konflikt' (Collision)?", List.of(new AnswerTemplate("Wenn zwei verschiedene Schlüssel den gleichen Hash-Wert ergeben.", true), new AnswerTemplate("Ein Fehler im Hash-Algorithmus.", false))),
                new QuestionTemplate("Was ist ein 'Primary Key' in einer Datenbanktabelle?", List.of(new AnswerTemplate("Ein eindeutiger Identifikator für einen Datensatz.", true), new AnswerTemplate("Ein Verweis auf eine andere Tabelle.", false))),
                new QuestionTemplate("Was ist das OSI-Modell?", List.of(new AnswerTemplate("Ein Referenzmodell für Netzwerkprotokolle in 7 Schichten.", true), new AnswerTemplate("Ein Modell für die CPU-Architektur.", false))),
                new QuestionTemplate("Was ist 'Unit Testing'?", List.of(new AnswerTemplate("Das Testen einzelner Komponenten oder Methoden einer Software.", true), new AnswerTemplate("Das Testen des gesamten Systems.", false)))
        );
    }
}