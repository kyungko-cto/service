package com.delivery.auth.controller;

import com.delivery.api.ApiResponse;
import com.delivery.auth.dto.*;
import com.delivery.auth.model.UserPrincipal;
import com.delivery.auth.service.AuthService;
import com.delivery.common.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 엔드포인트
 *
 * 엔드포인트:
 * - POST /auth/register → 회원가입
 * - POST /auth/login → 로그인
 * - POST /auth/refresh → 토큰 갱신
 * - GET /auth/me → 내 정보 조회
 * - DELETE /auth/me → 탈퇴
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     *
     * 검증:
     * - 이메일 중복 확인
     * - 비밀번호 암호화 (BCrypt)
     * - 요청 필드 검증 (@Valid)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        // 서비스: 회원가입 처리 (중복 확인, 암호화, 저장)
        UserResponse response = authService.register(request);

        return ApiResponse.success(response);
    }

    /**
     * 로그인
     *
     * 요청:
     * {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     *
     * 응답:
     * {
     *   "accessToken": "eyJhbGc...",
     *   "refreshToken": null  // 클라이언트에는 null 반환
     * }
     *
     * 쿠키 설정:
     * - refreshToken을 HttpOnly 쿠키에 저장
     * - 이유: XSS 공격으로부터 보호
     * - Secure: HTTPS 전송만 가능
     * - SameSite: CSRF 공격 방지
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        // 1. 서비스: 로그인 (이메일/비밀번호 검증, 토큰 생성)
        TokenResponse tokens = authService.login(request.getEmail(), request.getPassword());

        // 2. RefreshToken을 HttpOnly 쿠키에 저장
        //    이유: 자바스크립트로 접근 불가 (XSS 방어)
        Cookie refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());

        // HttpOnly: 자바스크립트 접근 불가 (XSS 방어)
        refreshCookie.setHttpOnly(true);

        // Secure: HTTPS 전송만 가능
        refreshCookie.setSecure(true);

        // Path: 모든 경로에서 전송
        refreshCookie.setPath("/");

        // MaxAge: 7일 (토큰 만료 시간과 동일)
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);

        // SameSite: CSRF 공격 방지 (Spring 6.1+)
        refreshCookie.setAttribute("SameSite", "Strict");

        response.addCookie(refreshCookie);

        // 3. 응답: AccessToken만 반환
        //    이유: RefreshToken은 쿠키에 안전하게 저장되므로 응답에 포함 불필요
        return ApiResponse.success(
                TokenResponse.builder()
                        .accessToken(tokens.getAccessToken())
                        .refreshToken(null)  // 명시적으로 null
                        .build()
        );
    }

    /**
     * AccessToken 갱신
     *
     * 동작:
     * 1. 쿠키에서 RefreshToken 추출
     * 2. RefreshToken 검증
     * 3. 새로운 AccessToken 발급
     *
     * 요청: GET /auth/refresh
     * (RefreshToken은 쿠키에 자동으로 포함)
     *
     * 응답:
     * {
     *   "accessToken": "eyJhbGc..."
     * }
     *
     * 쿠키 갱신:
     * - RefreshToken이 만료 임박이면 새로 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        // 1. RefreshToken 쿠키 확인
        if (refreshToken == null || refreshToken.isEmpty()) {
            // 타입 불일치 해결: ApiResponse.error()는 Void를 반환하므로 명시적으로 타입 지정
            ApiResponse<TokenResponse> errorResponse = new ApiResponse<>(
                    false,
                    ErrorCode.INVALID_PARAM.getCode(),
                    "RefreshToken이 없습니다. 다시 로그인하세요.",
                    null,
                    java.time.OffsetDateTime.now()
            );
            return ResponseEntity.status(ErrorCode.INVALID_PARAM.getHttpStatus())
                    .body(errorResponse);
        }

        try {
            // 2. 서비스: RefreshToken으로 새 토큰 발급
            TokenResponse tokens = authService.refreshByToken(refreshToken);

            // 3. RefreshToken 갱신 (만료 시간 연장)
            Cookie newRefreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
            newRefreshCookie.setHttpOnly(true);
            newRefreshCookie.setSecure(true);
            newRefreshCookie.setPath("/");
            newRefreshCookie.setMaxAge(7 * 24 * 60 * 60);
            newRefreshCookie.setAttribute("SameSite", "Strict");

            response.addCookie(newRefreshCookie);

            // 4. 응답: 새로운 AccessToken만 반환
            return ApiResponse.success(
                    TokenResponse.builder()
                            .accessToken(tokens.getAccessToken())
                            .refreshToken(null)
                            .build()
            );

        } catch (Exception e) {
            // RefreshToken 검증 실패 → 다시 로그인 필요

            // 쿠키 삭제 (MaxAge=0)
            Cookie deleteRefreshCookie = new Cookie("refreshToken", "");
            deleteRefreshCookie.setHttpOnly(true);
            deleteRefreshCookie.setSecure(true);
            deleteRefreshCookie.setPath("/");
            deleteRefreshCookie.setMaxAge(0);  // 즉시 삭제

            response.addCookie(deleteRefreshCookie);

            // 타입 불일치 해결: ApiResponse.error()는 Void를 반환하므로 명시적으로 타입 지정
            ApiResponse<TokenResponse> errorResponse = new ApiResponse<>(
                    false,
                    ErrorCode.INVALID_PARAM.getCode(),
                    "RefreshToken이 만료되었습니다. 다시 로그인하세요.",
                    null,
                    java.time.OffsetDateTime.now()
            );
            return ResponseEntity.status(ErrorCode.INVALID_PARAM.getHttpStatus()).body(errorResponse);
        }
    }

    /**
     * 내 정보 조회
     *
     * 인증 필수 (JWT 필요)
     *
     * @AuthenticationPrincipal: SecurityContext에서 UserPrincipal 자동 주입
     * JWT 필터에서 설정한 인증 정보를 받음
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal UserPrincipal principal) {

        // 1. 인증 확인
        if (principal == null) {
            // 타입 불일치 해결: ApiResponse.error()는 Void를 반환하므로 명시적으로 타입 지정
            ApiResponse<UserResponse> errorResponse = new ApiResponse<>(
                    false,
                    ErrorCode.UNAUTHORIZED.getCode(),
                    "인증이 필요합니다",
                    null,
                    java.time.OffsetDateTime.now()
            );
            return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getHttpStatus()).body(errorResponse);
        }

        // 2. UserPrincipal에서 정보 추출하여 응답 생성
        // 리팩토링: grade를 String에서 Grade enum으로 변환
        com.delivery.db.entity.user.Grade grade = null;
        if (principal.getGrade() != null) {
            try {
                grade = com.delivery.db.entity.user.Grade.valueOf(principal.getGrade());
            } catch (IllegalArgumentException e) {
                grade = com.delivery.db.entity.user.Grade.BRONZE; // 기본값
            }
        }
        
        UserResponse response = UserResponse.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .role(principal.getRole())
                .grade(grade)
                .build();

        return ApiResponse.success(response);
    }

    /**
     * 회원 탈퇴
     *
     * 동작:
     * 1. 사용자 정보 삭제
     * 2. Redis의 RefreshToken 삭제 (즉시 로그아웃)
     * 3. 쿠키 삭제
     *
     * 인증 필수 (JWT 필요)
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletResponse response) {

        // 1. 인증 확인
        if (principal == null) {
            return ApiResponse.error(
                    ErrorCode.UNAUTHORIZED,
                    "인증이 필요합니다"
            );
        }

        try {
            // 2. 서비스: 사용자 삭제 (DB 삭제 + Redis 토큰 삭제)
            authService.deleteUser(principal.getId());

            // 3. RefreshToken 쿠키 삭제
            Cookie deleteRefreshCookie = new Cookie("refreshToken", "");
            deleteRefreshCookie.setHttpOnly(true);
            deleteRefreshCookie.setSecure(true);
            deleteRefreshCookie.setPath("/");
            deleteRefreshCookie.setMaxAge(0);  // 즉시 삭제

            response.addCookie(deleteRefreshCookie);

            return ApiResponse.success(null);

        } catch (Exception e) {
            return ApiResponse.error(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "회원 탈퇴 중 오류가 발생했습니다"
            );
        }
    }
}