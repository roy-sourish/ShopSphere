package com.shopsphere.user.controller;

import com.shopsphere.user.domain.User;
import com.shopsphere.user.dto.CreateUserRequest;
import com.shopsphere.user.dto.UpdateUserRequest;
import com.shopsphere.user.dto.UserResponse;
import com.shopsphere.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Validated // enable validation for @PathVariable and @RequestParams
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a User <br>
     * POST /api/v1/users
     * @param request {email, password}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.getEmail(), request.getPassword());

        return UserResponse.from(user);
    }

    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UpdateUserRequest request
    ){
        User user = userService.updateUser(id, request);
        return UserResponse.from(user);
    }
    /**
     * Get User by ID <br>
     * GET /api/v1/users/:id
     * @param id existing user's id
     */
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable @Min(1) Long id) {
        User user = userService.getUserById(id);
        return UserResponse.from(user);
    }
    /**
     * Get User by Email <br>
     * GET /api/v1/users/email?email=email@example.com
     * @param email user email id
     */
    @GetMapping("/email")
    public UserResponse getUserByEmail(@RequestParam @Email String email){
        User user = userService.getUserByEmail(email);
        return UserResponse.from(user);
    }
}
