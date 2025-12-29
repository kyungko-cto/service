package com.delivery.auth.dto;

import com.delivery.db.entity.user.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JWT 토큰에 담을 사용자 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private Long id;          // 사용자 PK
    private String email;     // 사용자 이메일 (subject로 사용)
    private String role;      // 권한 (ROLE_USER, ROLE_ADMIN 등)
    private Grade grade;  // 사용자 등급 (기본 BRONZE, 결제 시 SILVER)
}
