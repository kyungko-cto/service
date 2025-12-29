package com.delivery.auth.service;

import com.delivery.auth.converter.UserConverter;
import com.delivery.auth.dto.*;
import com.delivery.auth.provider.JwtProvider;
import com.delivery.common.exception.DuplicateEmailException;
import com.delivery.common.exception.InvalidCredentialsException;
import com.delivery.db.entity.user.User;
import com.delivery.db.entity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }
        User user = UserConverter.toEntity(request, passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        return UserConverter.toResponse(saved);
    }

    // 로그인
    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        UserRequest userRequest = UserConverter.toRequest(user);
        String accessToken = jwtProvider.generateAccessToken(userRequest);
        String refreshToken = jwtProvider.generateRefreshToken(userRequest);

        return new TokenResponse(accessToken, refreshToken);
    }

    // Refresh Token 재발급
    public TokenResponse refreshByToken(String refreshToken) {
        jwtProvider.validateToken(refreshToken);
        UserRequest userRequest = UserConverter.toRequest(
                userRepository.findByEmail(jwtProvider.getClaims(refreshToken).getSubject())
                        .orElseThrow(InvalidCredentialsException::new)
        );
        String newAccessToken = jwtProvider.generateAccessToken(userRequest);
        String newRefreshToken = jwtProvider.generateRefreshToken(userRequest);
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    // 회원 삭제
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
