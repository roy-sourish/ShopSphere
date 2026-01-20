package com.shopsphere.user.repository;

import com.shopsphere.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /*
     * Query: SELECT * FROM users WHERE email = ? <br>
     * Optional<User>: Email lookup can find a user or find nothing.
     * Optional forces you to handle "not found" cases and avoid nullptr bugs <br>
     * Row exists → Optional.of(user) <br>
     * Row does not exist → Optional.empty()
     */

    /**
     * Find a User by Email
     * @param email user email id
     * */
    Optional<User> findByEmail(String email);
}
