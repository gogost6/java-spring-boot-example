package com.example.demo.dto;

import com.example.demo.entity.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        Set<Role> roles,
        LocalDateTime createdAt) {
}
