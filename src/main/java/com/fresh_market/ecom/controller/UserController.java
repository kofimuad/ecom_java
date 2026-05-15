package com.fresh_market.ecom.controller;

import com.fresh_market.ecom.model.User;
import com.fresh_market.ecom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "Manages user accounts")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users")
    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @Operation(summary = "Get a user by ID")
    @GetMapping("/{id}")
    public User getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Create a new user")
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @Operation(summary = "Update a user")
    @PutMapping("/{id}")
    public User updateUser(@PathVariable UUID id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @Operation(summary = "Delete a user")
    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable UUID id) {
        userService.deleteUser(id);
    }
}
