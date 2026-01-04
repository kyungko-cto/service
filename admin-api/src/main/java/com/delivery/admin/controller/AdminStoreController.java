package com.delivery.admin.controller;

import com.delivery.api.ApiResponse;
import com.delivery.admin.dto.StoreListResponse;
import com.delivery.admin.dto.StoreDetailResponse;
import com.delivery.admin.service.AdminStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 관리자 가게 관리 컨트롤러
 * 
 * 기능:
 * - 가게 목록 조회 (페이징, 상태 필터링)
 * - 가게 상세 조회
 * - 가게 상태 변경 (정지/활성화)
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
@RequestMapping("/api/admin/stores")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStoreController {

    private final AdminStoreService adminStoreService;

    /**
     * 가게 목록 조회
     * 
     * @param pageable 페이징 정보
     * @param status 가게 상태 필터 (선택)
     * @return 가게 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<StoreListResponse>>> getStores(
            Pageable pageable,
            @RequestParam(required = false) String status) {
        Page<StoreListResponse> stores = adminStoreService.getStores(pageable, status);
        return ApiResponse.success(stores);
    }

    /**
     * 가게 상세 조회
     * 
     * @param storeId 가게 ID
     * @return 가게 상세 정보
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStore(@PathVariable UUID storeId) {
        StoreDetailResponse store = adminStoreService.getStore(storeId);
        return ApiResponse.success(store);
    }

    /**
     * 가게 정지
     * 
     * @param storeId 가게 ID
     * @return 성공 메시지
     */
    @PostMapping("/{storeId}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendStore(@PathVariable UUID storeId) {
        adminStoreService.suspendStore(storeId);
        return ApiResponse.success();
    }

    /**
     * 가게 정지 해제
     * 
     * @param storeId 가게 ID
     * @return 성공 메시지
     */
    @PostMapping("/{storeId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateStore(@PathVariable UUID storeId) {
        adminStoreService.activateStore(storeId);
        return ApiResponse.success();
    }
}

