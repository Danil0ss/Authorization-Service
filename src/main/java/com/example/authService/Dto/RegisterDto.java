package com.example.authService.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDto(
        @NotBlank(message = "Email can't be empty")
        @Email
        String email,
        @NotBlank(message = "Password can't be empty")
        @Size(min = 6,message = "Password must contain at least 6 symbols")
        String password
) {
}
