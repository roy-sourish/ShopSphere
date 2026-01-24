package com.shopsphere.user.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    protected User(){
        // Required by JPA
    }

    public User(String email, String password){
        this.email = email;
        this.password = password;
        this.createdAt = Instant.now();
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

    // Domain-controlled updates

    public void changeEmail(String newEmail) {
        if(newEmail == null || newEmail.isBlank()){
            throw new IllegalArgumentException("Email cannot be blank");
        }
        this.email = newEmail;
    }

    public void changePassword(String hashedPassword) {
        if(hashedPassword == null || hashedPassword.isBlank()){
            throw new IllegalArgumentException("Email cannot be blank");
        }
        this.password = hashedPassword;
    }
}
