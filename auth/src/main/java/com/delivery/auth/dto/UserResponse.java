package com.delivery.auth.dto;

import com.delivery.db.entity.user.Grade;
import lombok.*;

import java.util.UUID;

/**
 * 사용자 응답 DTO
 * 
 * 리팩토링:
 * - id 타입을 Long에서 UUID로 변경하여 UserEntity와 일관성 확보
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;        // 사용자 PK (UUID로 변경)
    private String email;
    private String role;
    private Grade grade;
}
