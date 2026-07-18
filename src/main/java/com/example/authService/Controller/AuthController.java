package com.example.authService.Controller;

import com.example.authService.Dto.*;
import com.example.authService.Service.AuthService;
import com.example.authService.Service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.tokens.Token;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto){
        authService.register(registerDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginDto loginDto) {
        JwtResponse token = authService.login(loginDto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest refreshRequest){
        JwtResponse token=authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AddRoleRequest roleRequest){

        authService.addRoleToUser(userId,roleRequest.role());
        return ResponseEntity.ok().build();
    }
}
