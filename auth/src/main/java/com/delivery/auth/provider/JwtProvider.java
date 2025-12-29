package com.delivery.auth.provider;

import com.delivery.auth.dto.UserRequest;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserRequest userRequest) {
        return generateToken(userRequest, accessTokenExpirationMs);
    }

    public String generateRefreshToken(UserRequest userRequest) {
        return generateToken(userRequest, refreshTokenExpirationMs);
    }

    private String generateToken(UserRequest userRequest, long expirationMs) {
        return Jwts.builder()
                .setSubject(userRequest.getEmail())
                .claim("id", userRequest.getId())
                .claim("role", userRequest.getRole())
                .claim("grade", userRequest.getGrade().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public void validateToken(String token) {
        Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }
}
