package com.example.demo.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        String ownerEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
