package com.nibin.quiz.UserDTO;


import com.nibin.quiz.Model.Role;

public class RegisterResponseDTO {

    private String name;
    private Role role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public RegisterResponseDTO(String name, Role role) {
        this.name = name;
        this.role = role;
    }

    public RegisterResponseDTO() {
    }
}
