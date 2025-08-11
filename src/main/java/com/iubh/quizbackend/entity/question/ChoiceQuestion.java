package com.iubh.quizbackend.entity.question;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.iubh.quizbackend.entity.change.QuestionChangeRequest;
import com.iubh.quizbackend.entity.module.Module;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.util.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "choice_questions")
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"module", "answers", "changeRequests"}) // Exclude all relationships
public class ChoiceQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String questionText;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//        private ChoiceQuestionType questionType;


    @Builder.Default
    private int correctAnswerCount = 0;

    @Builder.Default
    private int incorrectAnswerCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;


    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @JsonManagedReference // This is correct to manage the "forward" part of the reference.
    private Set<Answer> answers = new HashSet<>();

    @Builder.Default
    @Embedded
    private ChangeRequestCounts changeRequestCounts = new ChangeRequestCounts();

    /**
     * --- IMPROVEMENT: Helper method for bidirectional relationship ---
     * This is a best practice to ensure that both sides of the relationship are correctly synchronized.
     *
     * @param answer The answer to add to this question.
     */
    public void addAnswer(Answer answer) {
        this.answers.add(answer);
        answer.setQuestion(this);
    }

    public void removeAnswer(Answer answer) {
        this.answers.remove(answer);
        answer.setQuestion(null);
    }


    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<QuestionChangeRequest> changeRequests = new HashSet<>();


    /**
     * Derives the question type based on the number of correct answers.
     * This is now pure logic and is not stored in the database.
     *
     * @return {@link ChoiceQuestionType#MULTI} if there is more than one correct answer,
     * otherwise {@link ChoiceQuestionType#SINGLE}.
     */
    public ChoiceQuestionType getQuestionType() {
        if (this.answers == null) {
            return ChoiceQuestionType.SINGLE; // Default for safety
        }

        long correctAnswersCount = this.answers.stream()
                .filter(Answer::getIsCorrect)
                .count();

        return correctAnswersCount > 1 ? ChoiceQuestionType.MULTI : ChoiceQuestionType.SINGLE;
    }


    @Embeddable
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeRequestCounts {

        @Column(name = "total")
        @Formula("(select count(qcr.id) from question_change_requests qcr where qcr.question_id = id)")
        private int total;

        // --- THE FIX IS APPLIED BELOW ---
        // We explicitly map the Java field to a snake_case column name for the formula.
        @Column(name = "question_text_change")
        @Formula("(select count(qcr.id) from question_change_requests qcr where qcr.question_id = id and qcr.request_type = 'INCORRECT_QUESTION_TEXT')")
        private int questionTextChange;

        @Column(name = "answer_change")
        @Formula("(select count(qcr.id) from question_change_requests qcr where qcr.question_id = id and qcr.request_type = 'INCORRECT_ANSWER')")
        private int answerChange;

        @Column(name = "duplication_change")
        @Formula("(select count(qcr.id) from question_change_requests qcr where qcr.question_id = id and qcr.request_type = 'DUPLICATE_QUESTION')")
        private int duplicationChange;

        @Column(name = "deletion_request")
        @Formula("(select count(qcr.id) from question_change_requests qcr where qcr.question_id = id and qcr.request_type = 'SUGGEST_DELETION')")
        private int deletionRequest;
    }

}
