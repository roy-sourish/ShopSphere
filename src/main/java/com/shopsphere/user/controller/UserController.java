package com.shopsphere.user.controller;

import com.shopsphere.user.domain.User;
import com.shopsphere.user.dto.CreateUserRequest;
import com.shopsphere.user.dto.UserResponse;
import com.shopsphere.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest request){
        User user = userService.createUser(request.getEmail(), request.getPassword());

        return  UserResponse.from(user);
    }
}
