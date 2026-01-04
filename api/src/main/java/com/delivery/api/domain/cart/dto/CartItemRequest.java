package com.delivery.api.domain.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 장바구니에 아이템 추가 시 요청 DTO
 *
 * 유효성 검증:
 * - productId: null이면 안 됨
 * - quantity: 1 이상이어야 함 (0개 이상은 의미 없음)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

    @NotNull(message = "상품 ID는 필수입니다")
    private UUID productId;

    @Positive(message = "수량은 1 이상이어야 합니다")
    private int quantity;
}