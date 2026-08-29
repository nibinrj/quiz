package com.nibin.quiz.Model;

import jakarta.persistence.*;
import lombok.Data; // Using Lombok for brevity, or generate getters/setters
import java.util.List;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Physics 101"

    private String description;

    // Track which Staff created this category
    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private Users createdBy;
}