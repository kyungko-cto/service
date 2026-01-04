package com.delivery.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 가게 목록 응답 DTO
 */
@Getter
@Builder
public class StoreListResponse {
    private UUID storeId;
    private String name;
    private String status;
    private String phone;
}

