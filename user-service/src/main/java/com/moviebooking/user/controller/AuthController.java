
package com.moviebooking.user.controller;

import com.moviebooking.contracts.dto.ApiResponse;
import com.moviebooking.user.dto.LoginRequest;
import com.moviebooking.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @Valid @RequestBody LoginRequest request) {

        String token = authService.login(request);

        ApiResponse<Map<String, String>> response =
                new ApiResponse<>(
                        true,
                        "Login successful",
                        Map.of("token", token, "tokenType", "Bearer")
                );

        return ResponseEntity.ok(response);
    }
}
