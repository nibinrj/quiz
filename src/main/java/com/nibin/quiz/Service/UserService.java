package com.nibin.quiz.Service;


import com.nibin.quiz.UserDTO.RegisterRequestDTO;
import com.nibin.quiz.UserDTO.RegisterResponseDTO;
import com.nibin.quiz.Model.Role;
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

    public RegisterResponseDTO register(RegisterRequestDTO userDTO )
    {
        Users user = new Users();
        user.setUsername(userDTO.getUsername());
        user.setPassword(crypt.encode(userDTO.getPassword()));
        user.setRole(Role.STUDENT);

        repo.save(user);

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setName(user.getUsername());
        response.setRole(user.getRole());
        return response;

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
