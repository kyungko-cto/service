package com.delivery.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 상세 응답 DTO
 */
@Getter
@Builder
public class UserDetailResponse {
    private UUID id;
    private String email;
    private String username;
    private String phoneNumber;
    private String role;
    private String grade;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

