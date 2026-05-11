package com.example.demo.controller;

import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public UserResponse getMe(Authentication authentication) {
        User user = authService.findByEmail(authentication.getName());
        return toResponse(user);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication authentication) {
        authService.deleteUser(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserByEmail(@PathVariable String email) {
        User user = authService.findByEmail(email);
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRoles(), user.getCreatedAt());
    }
}
