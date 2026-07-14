package com.example.authService.Dto;

public record ErrorResponse(
        String message,
        int status,
        long timestamp
) {}