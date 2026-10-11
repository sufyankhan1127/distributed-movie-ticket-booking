package com.moviebooking.user.controller;

import com.moviebooking.contracts.dto.ApiResponse;
import com.moviebooking.user.dto.RegisterRequest;
import com.moviebooking.user.entity.User;
import com.moviebooking.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        Map<String, Object> responseData = Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole()
        );

        ApiResponse<Map<String, Object>> response =
                new ApiResponse<>(true, "Registration successful", responseData);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}