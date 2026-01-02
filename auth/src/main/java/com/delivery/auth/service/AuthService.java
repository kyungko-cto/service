package com.delivery.auth.service;

import com.delivery.auth.converter.UserConverter;
import com.delivery.auth.dto.*;
import com.delivery.auth.provider.JwtProvider;
import com.delivery.common.exception.DuplicateEmailException;
import com.delivery.common.exception.InvalidCredentialsException;
import com.delivery.db.entity.user.User;
import com.delivery.db.entity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate; // Redis 추가

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }
        User user = UserConverter.toEntity(request, passwordEncoder.encode(request.getPassword()));
        return UserConverter.toResponse(userRepository.save(user));
    }

    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        UserRequest userRequest = UserConverter.toRequest(user);
        String accessToken = jwtProvider.generateAccessToken(userRequest);
        String refreshToken = jwtProvider.generateRefreshToken(userRequest);

        // 핵심: RefreshToken을 Redis에 저장하여 DB IO 병목 제거 (TTL 7일)
        redisTemplate.opsForValue().set(
                "RT:" + user.getId(),
                refreshToken,
                Duration.ofDays(7)
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refreshByToken(String refreshToken) {
        jwtProvider.validateToken(refreshToken);
        String email = jwtProvider.getClaims(refreshToken).getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        // Redis에서 토큰 검증 로직 추가 가능 (보안 강화)

        UserRequest userRequest = UserConverter.toRequest(user);
        return new TokenResponse(
                jwtProvider.generateAccessToken(userRequest),
                jwtProvider.generateRefreshToken(userRequest)
        );
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        redisTemplate.delete("RT:" + userId); // 관련 토큰 즉시 삭제
    }
}