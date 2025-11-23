package com.nibin.quiz.Controller;


import com.nibin.quiz.Model.Users;
import com.nibin.quiz.Repository.UserRepo;
import com.nibin.quiz.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
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
        //System.out.println(user.toString());
        return service.verify(user);
    }

    @PostMapping("/register")
    public Users register(@RequestBody Users users) {
        System.out.println(users.toString());

        return service.register(users);
    }
}
