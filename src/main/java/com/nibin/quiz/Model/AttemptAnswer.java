package com.nibin.quiz.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private QuizAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    private String selectedOption; // The string the student clicked
    private boolean isCorrect;     // Computed at submission time
}