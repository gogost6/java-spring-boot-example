package com.georgistoilkov.catify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank @Size(min = 1, max = 100) String title,
        @NotBlank @Size(min = 1, max = 10000) String body
) {}