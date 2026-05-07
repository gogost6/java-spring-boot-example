package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRequest authRequest) {
        User user = authService.register(authRequest);
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest authRequest) {
        User user = authService.login(authRequest);
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

    @PutMapping("/email")
    public AuthResponse updateEmail(Authentication authentication, @RequestParam String newEmail) {
        String email = authentication.getName();
        User user = authService.updateEmail(email, newEmail);
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

    @PutMapping("/password")
    public AuthResponse updatePassword(Authentication authentication,
                               @RequestParam String oldPassword,
                               @RequestParam String newPassword) {
        String email = authentication.getName();
        User user = authService.updatePassword(email, oldPassword, newPassword);
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

    @PutMapping("/users/{email}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public User addRoleToUser(
            @PathVariable String email,
            @PathVariable Role role
    ) {
        return authService.addRole(email, role);
    }
}