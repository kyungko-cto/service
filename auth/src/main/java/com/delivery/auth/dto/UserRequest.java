package com.delivery.auth.dto;

import com.delivery.db.entity.user.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * JWT 토큰에 담을 사용자 정보 DTO
 * 
 * 리팩토링:
 * - id 타입을 Long에서 UUID로 변경하여 UserEntity와 일관성 확보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private UUID id;          // 사용자 PK (UUID로 변경)
    private String email;     // 사용자 이메일 (subject로 사용)
    private String role;      // 권한 (ROLE_USER, ROLE_ADMIN 등)
    private Grade grade;      // 사용자 등급 (기본 BRONZE, 결제 시 SILVER)
}
