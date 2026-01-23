package com.shopsphere.user.exception;

public class DuplicateUserException extends RuntimeException{

    public DuplicateUserException(String email){
        super("User with '" + email + "' already exists");
    }
}
