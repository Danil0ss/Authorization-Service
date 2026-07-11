package com.example.authService.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtCore {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    // Вспомогательный метод для превращения строкового секрета в криптографический ключ
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes); // Метод библиотеки JJWT для генерации HMAC ключа
    }

    // 1. Генерация токена на основе UserDetails
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername()) // Записываем email (username) в Payload токена
                .issuedAt(now)                      // Время создания
                .expiration(expiryDate)             // Время окончания действия
                .signWith(getSigningKey())          // Подписываем токен нашим ключом
                .compact();                         // Собираем в строку
    }

    // 2. Получение имени пользователя (email) из токена
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey()) // Передаем ключ для проверки подписи
                .build()
                .parseSignedClaims(token)    // Парсим токен
                .getPayload();               // Забираем полезную нагрузку (Payload)

        return claims.getSubject();          // Возвращаем записанный subject (email)
    }

    // 3. Валидация токена (проверяем подпись и не истекло ли время действия)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);   // Если парсинг прошел без ошибок, токен валиден
            return true;
        } catch (Exception e) {
            // Сюда мы попадем, если токен просрочен, подпись не совпадает или токен поврежден
            return false;
        }
    }
}