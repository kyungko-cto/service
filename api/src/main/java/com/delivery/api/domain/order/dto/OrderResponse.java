package com.delivery.api.domain.order.dto;


import com.delivery.domain.order.type.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderResponse {
    private UUID orderId;
    private UUID userId;
    private UUID storeId;
    private OrderStatus status;
    private int totalAmount;
    private OffsetDateTime createdAt;
    private List<OrderLine> lines;

    @Getter
    @Builder
    public static class OrderLine {
        private UUID menuItemId;
        private String menuItemName;
        private int unitPrice;
        private int quantity;
        private int lineAmount;
    }
}
