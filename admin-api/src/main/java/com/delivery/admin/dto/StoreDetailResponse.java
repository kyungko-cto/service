package com.delivery.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 가게 상세 응답 DTO
 */
@Getter
@Builder
public class StoreDetailResponse {
    private UUID storeId;
    private String name;
    private String address;
    private String status;
    private String phone;
    private UUID ownerId;
}

