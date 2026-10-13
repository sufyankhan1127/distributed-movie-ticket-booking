package com.moviebooking.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestAuthController {

    @GetMapping("/api/users/test-auth")
    public Map<String, String> testAuth() {
        return Map.of(
                "message", "JWT authentication successful!"
        );
    }
}