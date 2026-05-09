package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(@NotEmpty @Size(min = 1, max = 5000) String content) { }
