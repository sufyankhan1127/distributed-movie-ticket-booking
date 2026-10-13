
package com.moviebooking.user.service;

import com.moviebooking.user.dto.LoginRequest;
import com.moviebooking.user.entity.User;
import com.moviebooking.user.repository.UserRepository;
import com.moviebooking.user.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(
                request.getEmail().trim().toLowerCase()
        ).orElseThrow(() ->
                new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return jwtService.generateToken(user.getEmail());
    }
}
