package com.delivery.api.domain.cart.converter;

import com.delivery.api.domain.cart.dto.CartItemRequest;
import com.delivery.api.domain.cart.dto.CartResponse;
import com.delivery.domain.cart.Cart;
import com.delivery.domain.cart.CartItem;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class CartConverter {

    /**
     * 요청 DTO → 도메인 엔티티 (CartItem)
     *
     * 이유: API 요청과 도메인 로직 분리
     * - 요청 필드명(productId)과 도메인 필드명(menuItemId)이 다를 수 있음
     * - 입력 검증과 변환을 한 곳에서 관리
     * - 향후 요청 DTO 변경 시 도메인에 영향 없음
     */
    public CartItem toDomain(CartItemRequest request) {
        // null 체크: 잘못된 요청이 들어왔을 때 NPE 방지
        if (request == null) {
            return null;
        }

        return CartItem.builder()
                // productId(요청)를 menuItemId(도메인)로 매핑
                .menuItemId(request.getProductId())
                .quantity(request.getQuantity())
                .build();
    }

    /**
     * 도메인 엔티티 (Cart) → 응답 DTO (CartResponse)
     *
     * 이유: 도메인 객체를 그대로 반환하지 않음
     * - 도메인의 모든 정보를 노출하지 않음 (민감한 데이터 숨김)
     * - 필요한 데이터만 선별해서 반환
     * - API 응답 구조와 도메인 모델이 독립적
     * - 향후 도메인 변경 시 API 호환성 유지 가능
     */
    public CartResponse toResponse(Cart cart) {
        // null 체크: 서비스에서 빈 Cart 반환 시에도 안전하게 처리
        if (cart == null) {
            return CartResponse.builder()
                    .items(Collections.emptyList())
                    .totalAmount(0)
                    .build();
        }

        // null 체크: cart.getItems()가 null일 가능성 대비
        List<CartResponse.CartLine> lines = cart.getItems() == null
                ? Collections.emptyList()
                : cart.getItems().stream()
                // CartItem → CartResponse.CartLine으로 변환
                .map(CartConverter::toCartLine)
                .toList();

        return CartResponse.builder()
                .userId(cart.getUserId())
                .storeId(cart.getStoreId())
                .totalAmount(cart.getTotalAmount())
                .items(lines)
                .build();
    }

    /**
     * CartItem → CartResponse.CartLine 변환 (추출된 헬퍼 메서드)
     *
     * 이유:
     * - toResponse 메서드를 간결하게 유지
     * - 아이템 변환 로직을 재사용 가능하게 분리
     * - 단일 책임 원칙: 한 메서드는 한 가지만 담당
     */
    private static CartResponse.CartLine toCartLine(CartItem item) {
        // null 체크: stream에서도 null이 포함될 가능성 대비
        if (item == null) {
            return null;
        }

        return CartResponse.CartLine.builder()
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                // lineAmount = unitPrice * quantity (서버에서 계산)
                // 이유: 클라이언트의 잘못된 계산 방지, 가격 조작 방지
                .lineAmount(item.getLineAmount())
                .build();
    }
}