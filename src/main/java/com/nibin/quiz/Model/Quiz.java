package com.nibin.quiz.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Data
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private QuizState state; // DRAFT, PUBLISHED

    private Integer timeLimitInMinutes;
    private LocalDateTime scheduledStartTime; // For cloud scheduling features

    // Link Quiz to a Category (e.g., This is a "Physics" quiz)
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;



    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
}