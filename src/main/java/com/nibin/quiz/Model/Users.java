package com.nibin.quiz.Model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ADDED THIS LINE
    private int Id;

    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

   
    private Set<Category> subscribedCategories;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Users(int id, String username, String password, Role role) {
        this.Id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Users() {
    }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + Id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }

    public Set<Category> getSubscribedCategories() {
        return subscribedCategories;
    }

    public void setSubscribedCategories(Set<Category> subscribedCategories) {
        this.subscribedCategories = subscribedCategories;
    }
}