package com.nibin.quiz.Service;


import com.nibin.quiz.Model.Users;
import com.nibin.quiz.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepo repo;

    @Autowired
    JWTservice jwt;

    @Autowired
    AuthenticationManager authenticationManager;


    private BCryptPasswordEncoder crypt =  new BCryptPasswordEncoder(12);

    public Users register(Users users )
    {
        users.setPassword(crypt.encode(users.getPassword()));
        repo.save(users);
        return users;

    }

    public String verify(Users user)
    {
        Authentication authentication =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword()));
        System.out.println("???00");
        if(authentication.isAuthenticated())
        {
            System.out.println("--------------------------------verify---------------------------");
            return jwt.generateToken(user.getUsername());
        }
        return "nuhhhh";
    }
}
