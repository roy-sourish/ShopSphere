package com.shopsphere.user.dto;

/**
 * This class represents: <br>
 * "The shape of the JSON that the client is allowed to send when creating a user." <br>
 * It's an API contract
 * */
public class CreateUserRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
