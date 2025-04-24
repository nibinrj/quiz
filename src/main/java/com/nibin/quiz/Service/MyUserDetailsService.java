package com.nibin.quiz.Service;

import com.nibin.quiz.Model.UserPrincipal;
import com.nibin.quiz.Model.Users;
import com.nibin.quiz.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService {

    UserRepo repo;

    @Autowired
    public MyUserDetailsService(UserRepo repo)
    {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user from the repository
        Users user = repo.findByusername(username);  // Assuming 'findByUsername' is the correct method name
        System.out.println(user.toString());
        // Handle case where user is not found
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // Return the custom UserPrincipal object with user details
        return new UserPrincipal(user);
    }
}
