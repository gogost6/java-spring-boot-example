package com.georgistoilkov.catify.dto;

public record AuthResponse(
                String token,
                String refreshToken) {
}