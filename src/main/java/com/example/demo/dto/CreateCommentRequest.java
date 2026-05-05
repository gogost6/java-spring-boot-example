package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;

public record CreateCommentRequest(@NotEmpty String content) { }
