package com.georgistoilkov.catify.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        String ownerEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
