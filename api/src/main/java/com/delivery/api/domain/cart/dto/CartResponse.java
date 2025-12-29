package com.delivery.api.domain.cart.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CartResponse {
    private UUID userId;
    private UUID storeId;
    private int totalAmount;
    private List<CartLine> items;

    @Getter
    @Builder
    public static class CartLine {
        private UUID menuItemId;
        private String menuItemName;
        private int unitPrice;
        private int quantity;
        private int lineAmount;
    }
}
