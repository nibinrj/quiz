package com.nibin.quiz.Controller;

import com.nibin.quiz.Model.Users;
import com.nibin.quiz.Service.UserService;
import com.nibin.quiz.DTO.RegisterRequestDTO;
import com.nibin.quiz.DTO.RegisterResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController("auth")
@RequestMapping
@CrossOrigin
@Slf4j
public class Usercontroller {

    @Autowired
    UserService service;

    @PostMapping("/login")
    public HashMap<String,String> login(@RequestBody Users user) {
        log.info("Login attempt for user: {}", user.getUsername());
        HashMap<String,String> map = new HashMap<>();
        map.put("token", service.verify(user));
        return map;
    }

    @PostMapping("/register")
    public RegisterResponseDTO register(@RequestBody RegisterRequestDTO user) {
        log.info("New registration request for user: {}", user.getUsername());
        return service.register(user);
    }
}