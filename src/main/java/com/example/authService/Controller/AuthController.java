package com.example.authService.Controller;

import com.example.authService.Dto.JwtResponse;
import com.example.authService.Dto.LoginDto;
import com.example.authService.Dto.RegisterDto;
import com.example.authService.Dto.TokenRefreshRequest;
import com.example.authService.Service.AuthService;
import com.example.authService.Service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.tokens.Token;

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
}
