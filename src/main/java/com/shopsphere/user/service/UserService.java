package com.shopsphere.user.service;

import com.shopsphere.user.domain.User;
import com.shopsphere.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User createUser(String email, String password){
        boolean exists = userRepository.findByEmail(email).isPresent();

        if(exists){
            throw new IllegalStateException("User with email already exists");
        }

        User user = new User(email, password);
        return userRepository.save(user);
    }
}
