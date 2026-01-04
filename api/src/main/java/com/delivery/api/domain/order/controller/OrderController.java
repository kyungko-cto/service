package com.delivery.api.domain.order.controller;

import com.delivery.api.ApiResponse;
import com.delivery.api.domain.order.dto.OrderResponse;
import com.delivery.api.domain.order.dto.CreateOrderRequest;
import com.delivery.api.domain.order.converter.OrderConverter;
import com.delivery.auth.model.UserPrincipal;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import com.delivery.db.entity.menu.MenuItemEntity;
import com.delivery.db.entity.menu.MenuItemRepository;
import com.delivery.domain.order.Order;
import com.delivery.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 주문 컨트롤러
 * 
 * 리팩토링 사항:
 * 1. 잘못된 import 경로 수정: com.delivery.order.converter -> com.delivery.api.domain.order.converter
 * 2. 존재하지 않는 메서드 호출 수정: createOrderOptimized -> create, getOrderDetails -> getById
 * 3. ResponseEntity 반환 타입 일관성 개선
 * 4. 주석 추가: 각 엔드포인트의 목적 설명
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final MenuItemRepository menuItemRepository;

    /**
     * 주문을 생성합니다.
     * 
     * @param user 인증된 사용자 정보
     * @param request 주문 생성 요청
     * @return 생성된 주문 정보
     * 
     * 리팩토링:
     * - 잘못된 import 경로 수정
     * - 존재하지 않는 메서드 호출 수정
     * - 201 Created 상태 코드 반환
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateOrderRequest request) {

        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "인증된 사용자 정보가 필요합니다");
        }

        // 리팩토링 완료: MenuItem을 조회하여 ItemLine 생성
        List<com.delivery.domain.order.ItemLine> domainItems = request.getItems().stream()
                .map(itemRequest -> {
                    MenuItemEntity menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                            .orElseThrow(() -> new BusinessException(
                                    ErrorCode.MENU_NOT_FOUND,
                                    "메뉴 아이템을 찾을 수 없습니다: " + itemRequest.getMenuItemId()
                            ));
                    
                    if (!menuItem.isAvailable()) {
                        throw new BusinessException(
                                ErrorCode.MENU_UNAVAILABLE,
                                "주문할 수 없는 메뉴입니다: " + menuItem.getName()
                        );
                    }
                    
                    return OrderConverter.toDomainItem(itemRequest, menuItem);
                })
                .collect(Collectors.toList());

        // 주문 생성
        UUID orderId = orderService.create(
                user.getId(),
                request.getStoreId(),
                domainItems
        );

        // 주문 조회하여 OrderResponse 생성
        Order order = orderService.getById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        OrderResponse response = OrderConverter.toResponse(order);

        URI location = URI.create("/api/orders/" + orderId);
        // 리팩토링: ResponseEntity를 직접 생성하여 location 헤더 추가
        ApiResponse<OrderResponse> apiResponse = new ApiResponse<>(
                true, "SUCCESS", "주문이 생성되었습니다", response, java.time.OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(apiResponse);
    }

    /**
     * 주문을 조회합니다.
     * 
     * @param orderUuid 주문 UUID
     * @return 주문 정보
     * 
     * 리팩토링:
     * - 존재하지 않는 메서드 호출 수정: getOrderDetails -> getById
     * - OrderResponse 변환 로직 추가 필요
     */
    @GetMapping("/{orderUuid}")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable UUID orderUuid) {
        var order = orderService.getById(orderUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        
        // 리팩토링 완료: OrderConverter를 사용하여 변환
        OrderResponse response = OrderConverter.toResponse(order);
        
        return ApiResponse.success(response);
    }
}
