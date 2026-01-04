package com.delivery.auth.provider;

import com.delivery.auth.dto.UserRequest;
import com.delivery.common.exception.JwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;


//oauth랑 jwt랑 차이
/**
 * JWT 토큰 생성 및 검증 컴포넌트
 *
 * 책임:
 * 1. AccessToken 생성 (만료: 15분)
 * 2. RefreshToken 생성 (만료: 7일, Redis에 저장)
 * 3. 토큰 검증 및 Claims 추출
 * 4. 예외 처리 (만료, 변조, 형식 오류)
 */
@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    /**
     * 서명 키 생성
     *
     * 이유:
     * - 동일한 키로 여러 번 생성하지 않도록 캐싱 권장
     * - 현재: 매번 생성하므로 최적화 가능 (향후 개선)
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * AccessToken 생성 (유효기간: 15분)
     *
     * 용도: API 요청 시 Authorization 헤더에 포함
     * 만료 시간이 짧아서 탈취되어도 피해 제한적
     */
    public String generateAccessToken(UserRequest userRequest) {
        return generateToken(userRequest, accessTokenExpirationMs);
    }

    /**
     * RefreshToken 생성 (유효기간: 7일)
     *
     * 용도:
     * - AccessToken 만료 시 새 AccessToken 발급
     * - Redis에 저장하여 로그아웃/토큰 폐기 지원
     * - 긴 유효기간으로 사용자 편의성 확보
     */
    public String generateRefreshToken(UserRequest userRequest) {
        return generateToken(userRequest, refreshTokenExpirationMs);
    }

    /**
     * 토큰 생성 (내부 헬퍼 메서드)
     *
     * 토큰 구조:
     * {
     *   "sub": "user@example.com",    // 이메일
     *   "id": 1,                       // 사용자 PK
     *   "role": "ROLE_USER",           // 권한
     *   "grade": "BRONZE",             // 등급
     *   "iat": 1234567890,             // 발급 시간
     *   "exp": 1234567890              // 만료 시간
     * }
     */
    private String generateToken(UserRequest userRequest, long expirationMs) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + expirationMs);

        return Jwts.builder()
                // Subject: 토큰의 주체 (일반적으로 사용자 이메일/ID)
                .setSubject(userRequest.getEmail())

                // 사용자 정보 클레임
                .claim("id", userRequest.getId())
                .claim("role", userRequest.getRole())
                .claim("grade", userRequest.getGrade().name())

                // 발급/만료 시간
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)

                // 서명 (토큰 변조 감지)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)

                // 토큰 문자열로 변환
                .compact();
    }

    /**
     * 토큰 유효성 검증
     *
     * 검증 항목:
     * 1. 서명 검증 (토큰 변조 감지)
     * 2. 만료 시간 검증
     * 3. 형식 검증 (JWT 형식 맞는지)
     *
     * @param token JWT 토큰
     * @throws JwtException 검증 실패 시
     */
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

        } catch (SecurityException | MalformedJwtException e) {
            // 서명 오류 또는 형식 오류
            log.error("잘못된 JWT 서명: {}", e.getMessage());
            throw new JwtException("유효하지 않은 토큰입니다");

        } catch (ExpiredJwtException e) {
            // 만료된 토큰
            log.error("만료된 JWT 토큰: {}", e.getMessage());
            throw new JwtException("토큰이 만료되었습니다");

        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 JWT
            log.error("지원되지 않는 JWT 형식: {}", e.getMessage());
            throw new JwtException("지원하지 않는 토큰 형식입니다");

        } catch (IllegalArgumentException e) {
            // 빈 문자열 등
            log.error("JWT 클레임 문자열이 비어있음: {}", e.getMessage());
            throw new JwtException("토큰이 비어있습니다");
        }
    }

    /**
     * 토큰에서 클레임(사용자 정보) 추출
     *
     * 호출 전 반드시 validateToken()으로 검증 필수
     *
     * @param token JWT 토큰
     * @return 토큰에 포함된 모든 클레임
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (JwtException e) {
            log.error("JWT 클레임 추출 실패: {}", e.getMessage());
            throw new JwtException("토큰 정보를 추출할 수 없습니다");
        }
    }

    /**
     * 토큰에서 사용자 ID 추출 (편의 메서드)
     * 
     * 리팩토링:
     * - 반환 타입을 Long에서 UUID로 변경하여 UserEntity와 일관성 확보
     */
    public UUID getUserIdFromToken(String token) {
        Object idClaim = getClaims(token).get("id");
        if (idClaim instanceof UUID) {
            return (UUID) idClaim;
        } else if (idClaim instanceof String) {
            return UUID.fromString((String) idClaim);
        } else {
            throw new IllegalArgumentException("토큰에서 사용자 ID를 추출할 수 없습니다");
        }
    }

    /**
     * 토큰에서 이메일 추출 (편의 메서드)
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰이 만료되었는지 확인
     *
     * @param token JWT 토큰
     * @return true면 만료됨
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}