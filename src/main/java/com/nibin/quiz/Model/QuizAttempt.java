package com.nibin.quiz.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Using UUID for high-volume attempts
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users student;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private Integer score;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Track every single answer the student gave
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL)
    private List<AttemptAnswer> answers;
}