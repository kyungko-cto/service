package com.delivery.api.domain.order.converter;

import com.delivery.api.domain.order.dto.OrderItemRequest;
import com.delivery.api.domain.order.dto.OrderResponse;
import com.delivery.db.entity.menu.MenuItemEntity;
import com.delivery.domain.order.ItemLine;
import com.delivery.domain.order.Order;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 관련 변환기
 * 
 * 리팩토링 완료:
 * - MenuItemEntity를 사용하여 menuItemName과 unitPrice를 가져오는 메서드 추가
 * - Order를 OrderResponse로 변환하는 메서드 추가
 */
public class OrderConverter {
    
    /**
     * OrderItemRequest와 MenuItemEntity를 사용하여 ItemLine 도메인 객체로 변환합니다.
     * 
     * @param request 주문 아이템 요청 DTO
     * @param menuItem 메뉴 아이템 엔티티
     * @return 주문 아이템 라인 도메인 객체
     * 
     * 리팩토링 완료:
     * - MenuItemEntity를 파라미터로 받아서 menuItemName과 unitPrice를 가져옴
     */
    public static ItemLine toDomainItem(OrderItemRequest request, MenuItemEntity menuItem) {
        if (request == null) {
            throw new IllegalArgumentException("주문 아이템 요청은 null일 수 없습니다");
        }
        if (menuItem == null) {
            throw new IllegalArgumentException("메뉴 아이템은 null일 수 없습니다");
        }
        
        return new ItemLine(
                request.getMenuItemId(),
                menuItem.getName(),
                menuItem.getPrice(),
                request.getQuantity()
        );
    }
    
    /**
     * Order 도메인 객체를 OrderResponse DTO로 변환합니다.
     * 
     * @param order 주문 도메인 객체
     * @return 주문 응답 DTO
     * 
     * 리팩토링 완료:
     * - Order의 details를 OrderResponse의 lines로 변환
     */
    public static OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        
        List<OrderResponse.OrderLine> lines = order.getDetails() == null
                ? Collections.emptyList()
                : order.getDetails().stream()
                .map(item -> OrderResponse.OrderLine.builder()
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .lineAmount(item.getLineAmount())
                        .build())
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .storeId(order.getStoreId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .lines(lines)
                .build();
    }
}
