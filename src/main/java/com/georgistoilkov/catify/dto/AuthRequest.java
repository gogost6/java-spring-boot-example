package com.georgistoilkov.catify.dto;

import jakarta.validation.constraints.*;

public record AuthRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 12, max = 60)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{12,60}$")
        String password
) {
}
