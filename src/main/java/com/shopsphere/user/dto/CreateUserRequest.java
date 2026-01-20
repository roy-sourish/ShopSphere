package com.shopsphere.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * This class represents: <br>
 * "The shape of the JSON that the client is allowed to send when creating a user." <br>
 * It's an API contract
 * */
public class CreateUserRequest {
    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
