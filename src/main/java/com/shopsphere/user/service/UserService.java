package com.shopsphere.user.service;

import com.shopsphere.common.exception.OptimisticConflictException;
import com.shopsphere.user.domain.User;
import com.shopsphere.user.dto.UpdateUserRequest;
import com.shopsphere.user.exception.DuplicateUserException;
import com.shopsphere.user.exception.UserNotFoundException;
import com.shopsphere.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a User - service method
     *
     * @param email       User email
     * @param rawPassword User Password
     */
    @Transactional
    public User createUser(String email, String rawPassword) {
        // Hash password before saving
        String hashedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(email, hashedPassword);

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateUserException(email);
        }
    }

    /**
     * Update User Credentials - service method
     */
    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request) {
        // Step 1: Load user or fail
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Step 2: Apply patch updates safely
        if (request.getEmail() != null) {
            user.changeEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            String newHashedPassword = passwordEncoder.encode(request.getPassword());
            user.changePassword(newHashedPassword);
        }

        // Step 3: Force DB constraint + version check now
        try{
            userRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateUserException(request.getEmail());
        } catch (ObjectOptimisticLockingFailureException ex){
            throw new OptimisticConflictException("User",userId);
        }

        return user;
    }

    /**
     * Get User by Id - service method
     *
     * @param userId (Long) user id
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        // findById returns Optional<User>
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Get User by Email - service method
     *
     * @param email (String) email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}
