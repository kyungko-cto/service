package com.delivery.admin.controller;

import com.delivery.api.ApiResponse;
import com.delivery.admin.dto.UserListResponse;
import com.delivery.admin.dto.UserDetailResponse;
import com.delivery.admin.service.AdminUserService;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 관리자 사용자 관리 컨트롤러
 * 
 * 기능:
 * - 사용자 목록 조회 (페이징)
 * - 사용자 상세 조회
 * - 사용자 상태 변경 (활성화/비활성화)
 * - 사용자 등급 변경
 * 
 * 리팩토링 사항:
 * 1. @PreAuthorize로 관리자 권한 검증 (클래스 레벨)
 * 2. 응답 타입 명확화: ApiResponse.success() 사용 (데이터 없을 때)
 * 3. 주석 추가: 각 엔드포인트의 목적과 사용 예시
 * 
 * 보안:
 * - 모든 엔드포인트는 ADMIN 역할만 접근 가능
 * - Spring Security의 @PreAuthorize로 권한 검증
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 사용자 목록 조회 (페이징)
     * 
     * @param pageable 페이징 정보
     * @return 사용자 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserListResponse>>> getUsers(Pageable pageable) {
        Page<UserListResponse> users = adminUserService.getUsers(pageable);
        return ApiResponse.success(users);
    }

    /**
     * 사용자 상세 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 상세 정보
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUser(@PathVariable UUID userId) {
        UserDetailResponse user = adminUserService.getUser(userId);
        return ApiResponse.success(user);
    }

    /**
     * 사용자 계정 활성화
     * 
     * @param userId 사용자 ID
     * @return 성공 메시지
     */
    @PostMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID userId) {
        adminUserService.activateUser(userId);
        return ApiResponse.success();
    }

    /**
     * 사용자 계정 비활성화
     * 
     * @param userId 사용자 ID
     * @return 성공 메시지
     */
    @PostMapping("/{userId}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable UUID userId) {
        adminUserService.suspendUser(userId);
        return ApiResponse.success();
    }

    /**
     * 사용자 등급 변경
     * 
     * @param userId 사용자 ID
     * @param grade 새로운 등급
     * @return 성공 메시지
     */
    @PutMapping("/{userId}/grade")
    public ResponseEntity<ApiResponse<Void>> changeGrade(
            @PathVariable UUID userId,
            @RequestParam String grade) {
        adminUserService.changeGrade(userId, grade);
        return ApiResponse.success();
    }
}

