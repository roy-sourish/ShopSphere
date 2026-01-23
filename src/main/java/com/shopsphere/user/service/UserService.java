package com.shopsphere.user.service;

import com.shopsphere.user.domain.User;
import com.shopsphere.user.exception.DuplicateUserException;
import com.shopsphere.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String email, String rawPassword){
        if(userRepository.findByEmail(email).isPresent()){
            throw new DuplicateUserException(email);
        }

        // Hash password before saving
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(email, hashedPassword);

        return userRepository.save(user);
    }
}
