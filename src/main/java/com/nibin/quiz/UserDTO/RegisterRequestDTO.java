package com.nibin.quiz.UserDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequestDTO {

    @NotBlank(message = "username is required")
    private String username ;

    @NotBlank(message = "password is required")
    @Size(min = 6 , message = "minimum 6 characters is required ")
    private String password ;

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

    public RegisterRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
