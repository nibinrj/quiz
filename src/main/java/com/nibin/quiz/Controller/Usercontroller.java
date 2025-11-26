package com.nibin.quiz.Controller;


import com.nibin.quiz.UserDTO.RegisterRequestDTO;
import com.nibin.quiz.UserDTO.RegisterResponseDTO;
import com.nibin.quiz.Model.Users;

import com.nibin.quiz.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping
public class Usercontroller {

    @Autowired
    UserService service;

    @PostMapping("/login")
    public String login(@RequestBody Users user)
    {
        return service.verify(user);
    }

    @PostMapping("/register")
    public RegisterResponseDTO register(@RequestBody RegisterRequestDTO user) {
        return service.register(user);
    }
}
