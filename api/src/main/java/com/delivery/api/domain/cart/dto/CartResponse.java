package com.delivery.api.domain.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 장바구니 조회 응답 DTO
 *
 * @JsonProperty: JSON 직렬화 시 필드명 커스터마이징
 * - DB와 API 필드명이 다를 때 사용
 * - 예: userId → user_id (스네이크 케이스)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("store_id")
    private UUID storeId;

    @JsonProperty("total_amount")
    private int totalAmount;

    private List<CartLine> items;

    /**
     * 장바구니 내 개별 아이템 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartLine {

        @JsonProperty("menu_item_id")
        private UUID menuItemId;

        @JsonProperty("menu_item_name")
        private String menuItemName;

        @JsonProperty("unit_price")
        private int unitPrice;

        private int quantity;

        @JsonProperty("line_amount")
        private int lineAmount;
    }
}