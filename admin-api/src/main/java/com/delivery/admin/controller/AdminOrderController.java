package com.delivery.admin.controller;

import com.delivery.api.ApiResponse;
import com.delivery.admin.dto.OrderListResponse;
import com.delivery.admin.dto.OrderDetailResponse;
import com.delivery.admin.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 관리자 주문 관리 컨트롤러
 * 
 * 기능:
 * - 주문 목록 조회 (페이징, 필터링)
 * - 주문 상세 조회
 * - 주문 취소 (관리자 권한)
 * 
 * 리팩토링 사항:
 * 1. @PreAuthorize로 관리자 권한 검증 (클래스 레벨)
 * 2. 응답 타입 명확화: ApiResponse.success() 사용 (데이터 없을 때)
 * 3. 주석 추가: 각 엔드포인트의 목적과 사용 예시
 * 4. 필터링 파라미터 개선: 날짜 형식 명시
 * 
 * 보안:
 * - 모든 엔드포인트는 ADMIN 역할만 접근 가능
 * - Spring Security의 @PreAuthorize로 권한 검증
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /**
     * 주문 목록 조회
     * 
     * @param pageable 페이징 정보
     * @param status 주문 상태 필터 (선택)
     * @param startDate 시작 날짜 (선택)
     * @param endDate 종료 날짜 (선택)
     * @return 주문 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getOrders(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        Page<OrderListResponse> orders = adminOrderService.getOrders(pageable, status, startDate, endDate);
        return ApiResponse.success(orders);
    }

    /**
     * 주문 상세 조회
     * 
     * @param orderId 주문 ID
     * @return 주문 상세 정보
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(@PathVariable UUID orderId) {
        OrderDetailResponse order = adminOrderService.getOrder(orderId);
        return ApiResponse.success(order);
    }

    /**
     * 주문 취소
     * 
     * @param orderId 주문 ID
     * @return 성공 메시지
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable UUID orderId) {
        adminOrderService.cancelOrder(orderId);
        return ApiResponse.success();
    }
}

