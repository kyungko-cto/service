package com.delivery.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 주문 상세 응답 DTO
 */
@Getter
@Builder
public class OrderDetailResponse {
    private UUID orderId;
    private UUID userId;
    private UUID storeId;
    private String status;
    private int totalAmount;
    private OffsetDateTime createdAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime completedAt;
}

