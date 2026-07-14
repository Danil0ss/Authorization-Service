package com.example.authService.Service;

import com.example.authService.Entity.RefreshToken;
import com.example.authService.Entity.User;
import com.example.authService.Exception.TokenRefreshException;
import com.example.authService.Exception.UserNotFoundException;
import com.example.authService.Repository.RefreshTokenRepository;
import com.example.authService.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration}")
    private int jwtRefreshExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(UUID userId){
        User user=userRepository.findById(userId).
                orElseThrow(()->new UserNotFoundException("Пользователь не найден"));

        String token=UUID.randomUUID().toString();
        Instant expiryDate= Instant.now().plusMillis(jwtRefreshExpirationMs);

        RefreshToken refreshToken=new RefreshToken(null,token,expiryDate,user);

        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    @Transactional
    public RefreshToken verifyExpiration (RefreshToken token){
        if(token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Токен просрочен");
        }
        else return token;
    }
}
