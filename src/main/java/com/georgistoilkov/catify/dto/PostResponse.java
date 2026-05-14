package com.georgistoilkov.catify.dto;

public record PostResponse(
        Long id,
        String title,
        String body,
        String ownerEmail
) { }
