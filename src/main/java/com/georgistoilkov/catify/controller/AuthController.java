package com.georgistoilkov.catify.controller;

import com.georgistoilkov.catify.dto.AuthRequest;
import com.georgistoilkov.catify.dto.AuthResponse;
import com.georgistoilkov.catify.dto.RefreshRequest;
import com.georgistoilkov.catify.entity.RefreshToken;
import com.georgistoilkov.catify.entity.Role;
import com.georgistoilkov.catify.entity.User;
import com.georgistoilkov.catify.service.AuthService;
import com.georgistoilkov.catify.service.JwtService;
import com.georgistoilkov.catify.service.RefreshTokenService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRequest authRequest) {
        User user = authService.register(authRequest);
        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(token, refreshToken.getToken());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest authRequest) {
        User user = authService.login(authRequest);
        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(token, refreshToken.getToken());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        User user = refreshTokenService.validateRefreshToken(request.refreshToken());
        String newAccessToken = jwtService.generateToken(user);

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        User user = authService.findByEmail(authentication.getName());
        refreshTokenService.revokeTokensForUser(user);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/email")
    public AuthResponse updateEmail(Authentication authentication, @RequestParam String newEmail) {
        String email = authentication.getName();
        User user = authService.updateEmail(email, newEmail);
        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(token, refreshToken.getToken());
    }

    @PutMapping("/password")
    public AuthResponse updatePassword(Authentication authentication,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        String email = authentication.getName();
        User user = authService.updatePassword(email, oldPassword, newPassword);
        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(token, refreshToken.getToken());
    }

    @PutMapping("/users/{email}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public User addRoleToUser(
            @PathVariable String email,
            @PathVariable Role role) {
        return authService.addRole(email, role);
    }
}