package com.nibin.quiz.Model;

import jakarta.persistence.*;

@Entity
public class CategorySubscriptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToMany
    @JoinColumn(referencedColumnName = "Id")
    private Users studentId;

    @ManyToMany
    @JoinColumn(referencedColumnName = "Id")
    private Category categoryId;

    public CategorySubscriptions(int id, Users studentId, Category categoryId) {
        this.id = id;
        this.studentId = studentId;
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CategorySubscriptions() {
    }

    public Users getStudentId() {
        return studentId;
    }

    public void setStudentId(Users studentId) {
        this.studentId = studentId;
    }

    public Category getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }

}
