package com.nibin.quiz.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "category_subscriptions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "category_id"}))
public class CategorySubscriptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Users student;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}