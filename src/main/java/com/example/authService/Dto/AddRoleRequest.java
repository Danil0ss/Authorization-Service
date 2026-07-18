package com.example.authService.Dto;

import com.example.authService.Entity.Role;
import jakarta.validation.constraints.NotBlank;

public record AddRoleRequest(
        @NotBlank(message = "Role cannot be empty")
        Role role
) {
}
