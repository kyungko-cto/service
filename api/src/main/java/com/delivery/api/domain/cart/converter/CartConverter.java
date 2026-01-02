package com.delivery.api.domain.cart.converter;



import com.delivery.api.domain.cart.dto.CartItemRequest;
import com.delivery.api.domain.cart.dto.CartResponse;
import com.delivery.domain.cart.Cart;
import com.delivery.domain.cart.CartItem;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class CartConverter {

    /**
     * 요청 DTO → 도메인 엔티티 (CartItem)
     */
    public CartItem toDomain(CartItemRequest request) {
        if (request == null) return null;
        return CartItem.builder()
                .menuItemId(request.getProductId())
                .quantity(request.getQuantity())
                .build();
    }

    /**
     * 도메인 엔티티 (Cart) → 응답 DTO (CartResponse)
     */
    public CartResponse toResponse(Cart cart) {
        if (cart == null) return null;

        List<CartResponse.CartLine> lines = cart.getItems().stream()
                .map(item -> CartResponse.CartLine.builder()
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .lineAmount(item.getLineAmount())
                        .build())
                .toList();

        return CartResponse.builder()
                .userId(cart.getUserId())
                .storeId(cart.getStoreId())
                .totalAmount(cart.getTotalAmount())
                .items(lines)
                .build();
    }
}
