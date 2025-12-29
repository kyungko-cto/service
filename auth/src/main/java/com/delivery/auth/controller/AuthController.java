package com.delivery.auth.controller;

import com.delivery.api.ApiResponse;
import com.delivery.auth.dto.*;
import com.delivery.auth.model.UserPrincipal;
import com.delivery.auth.service.AuthService;
import com.delivery.db.entity.user.Grade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest request,
                                                            HttpServletResponse response) {
        TokenResponse tokens = authService.login(request.getEmail(), request.getPassword());

        Cookie refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        return ApiResponse.success(new TokenResponse(tokens.getAccessToken(), null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ApiResponse.error(com.delivery.common.exception.ErrorCode.INVALID_PARAM, "리프레시 토큰이 없습니다.");
        }

        TokenResponse tokens = authService.refreshByToken(refreshToken);
        return ApiResponse.success(new TokenResponse(tokens.getAccessToken(), null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal UserPrincipal principal) {
        authService.deleteUser(principal.getId());
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = UserResponse.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .role(principal.getRole())
                .grade(Grade.valueOf(principal.getGrade()))
                .build();
        return ApiResponse.success(response);
    }
}
