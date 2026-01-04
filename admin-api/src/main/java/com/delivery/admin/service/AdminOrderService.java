package com.delivery.admin.service;

import com.delivery.admin.dto.OrderListResponse;
import com.delivery.admin.dto.OrderDetailResponse;
import com.delivery.db.entity.order.OrderEntity;
import com.delivery.db.entity.order.OrderRepository;
import com.delivery.service.order.OrderService;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 관리자 주문 관리 서비스
 * 
 * 리팩토링 사항:
 * 1. application 모듈의 OrderService 재사용: 주문 취소 등 공통 로직 재사용
 *    이유: 비즈니스 로직 중복 방지 및 일관성 유지
 * 2. 필터링 로직 개선: Specification을 사용한 동적 쿼리 (향후 구현)
 * 3. 로깅 추가: 관리자 작업 추적
 * 4. DTO 변환 로직 개선: null 안전성 강화
 * 5. 주석 추가: 각 메서드의 목적과 비즈니스 규칙 설명
 * 
 * 설계 원칙:
 * - application 모듈의 서비스를 재사용하여 비즈니스 로직 중복 방지
 * - 관리자 전용 기능(목록 조회, 필터링)은 독립적으로 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * 주문 목록 조회 (필터링 지원)
     * 
     * @param pageable 페이징 정보
     * @param status 주문 상태 필터 (선택)
     * @param startDate 시작 날짜 (선택)
     * @param endDate 종료 날짜 (선택)
     * @return 주문 목록 (페이징)
     * 
     * 리팩토링 사항:
     * - 문제: 현재는 필터링이 구현되지 않음
     * - 해결: 향후 Specification을 사용하여 동적 쿼리 구현 예정
     *   예: status가 있으면 상태 필터링, 날짜 범위 필터링 등
     */
    public Page<OrderListResponse> getOrders(
            Pageable pageable,
            String status,
            OffsetDateTime startDate,
            OffsetDateTime endDate) {
        
        // TODO: Specification을 사용하여 동적 쿼리 구성
        // 예시:
        // Specification<OrderEntity> spec = Specification.where(null);
        // if (status != null) {
        //     spec = spec.and((root, query, cb) -> 
        //         cb.equal(root.get("status"), OrderStatus.valueOf(status)));
        // }
        // if (startDate != null && endDate != null) {
        //     spec = spec.and((root, query, cb) -> 
        //         cb.between(root.get("createdAt"), startDate, endDate));
        // }
        // return orderRepository.findAll(spec, pageable).map(this::toListResponse);
        
        // 현재는 간단한 예시로 전체 조회
        Page<OrderEntity> orders = orderRepository.findAll(pageable);
        return orders.map(this::toListResponse);
    }

    /**
     * 주문 상세 조회
     * 
     * @param orderId 주문 ID
     * @return 주문 상세 정보
     * @throws BusinessException 주문을 찾을 수 없을 때
     */
    public OrderDetailResponse getOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return toDetailResponse(order);
    }

    /**
     * 주문 취소 (관리자 권한)
     * 
     * @param orderId 주문 ID
     * @throws BusinessException 주문을 찾을 수 없거나 취소할 수 없는 상태일 때
     * 
     * 리팩토링 사항:
     * - application 모듈의 OrderService.cancel() 재사용
     *   이유: 주문 취소 비즈니스 로직은 일반 사용자와 동일하므로 재사용
     * - 로깅: 관리자 작업 추적을 위한 로그 추가
     */
    @Transactional
    public void cancelOrder(UUID orderId) {
        orderService.cancel(orderId);
        log.info("관리자 작업: 주문 취소 - orderId: {}", orderId);
    }

    /**
     * OrderEntity를 OrderListResponse로 변환합니다.
     * 
     * @param order 주문 엔티티
     * @return 주문 목록 응답 DTO
     * 
     * 리팩토링: null 안전성 강화 (관계 엔티티와 enum이 null일 수 있는 경우 대비)
     */
    private OrderListResponse toListResponse(OrderEntity order) {
        return OrderListResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .storeId(order.getStore() != null ? order.getStore().getId() : null)
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * OrderEntity를 OrderDetailResponse로 변환합니다.
     * 
     * @param order 주문 엔티티
     * @return 주문 상세 응답 DTO
     * 
     * 리팩토링: null 안전성 강화 (관계 엔티티와 enum이 null일 수 있는 경우 대비)
     */
    private OrderDetailResponse toDetailResponse(OrderEntity order) {
        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .storeId(order.getStore() != null ? order.getStore().getId() : null)
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
}

