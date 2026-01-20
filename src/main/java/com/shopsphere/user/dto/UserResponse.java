package com.shopsphere.user.dto;

import com.shopsphere.user.domain.User;

import java.time.Instant;

/**
 * "The exact shape of JSON the server is willing to expose"
 * */
public class UserResponse {
    private Long id;
    private String email;
    private Instant createdAt;

    public UserResponse(Long id, String email, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.createdAt = createdAt;
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
