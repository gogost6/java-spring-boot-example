package com.georgistoilkov.catify.dto;

import com.georgistoilkov.catify.entity.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        Set<Role> roles,
        LocalDateTime createdAt) {
}
