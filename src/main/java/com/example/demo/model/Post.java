package com.example.demo.model;

public record Post(
    int userId,
    int id,
    String title,
    String body
) {}