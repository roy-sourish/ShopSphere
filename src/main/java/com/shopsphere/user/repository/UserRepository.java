package com.shopsphere.user.repository;

import com.shopsphere.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find a User by Email
     * @param email user email id
     * */
    Optional<User> findByEmail(String email);
}
