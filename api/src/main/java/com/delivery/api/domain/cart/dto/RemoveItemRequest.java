package com.delivery.api.domain.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 장바구니에서 아이템 제거 시 요청 DTO
 *
 * 리팩토링:
 * - getter 메서드 추가 (request.menuItemId() -> request.getMenuItemId())
 * - record로 변경하는 것도 고려 가능
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RemoveItemRequest {

    @NotNull(message = "상품 ID는 필수입니다")
    private UUID menuItemId;
}