package com.delivery.auth.converter;

import com.delivery.auth.dto.RegisterRequest;
import com.delivery.auth.dto.UserRequest;
import com.delivery.auth.dto.UserResponse;
import com.delivery.db.entity.user.UserEntity;
import com.delivery.db.entity.user.Role;
import com.delivery.db.entity.user.Grade;

/**
 * 사용자 변환기
 * 
 * 리팩토링 사항:
 * 1. 잘못된 import 수정: User -> UserEntity
 * 2. password 필드명 수정: password -> passwordHash
 * 3. Role enum 사용: String 대신 Role enum 사용
 * 4. 주석 추가: 각 메서드의 목적 설명
 */
public class UserConverter {

    /**
     * UserEntity를 UserRequest로 변환합니다.
     * 
     * @param user 사용자 엔티티
     * @return 사용자 요청 DTO
     * 
     * 리팩토링 완료:
     * - UserEntity와 UserRequest 모두 UUID를 사용하므로 직접 매핑 가능
     * - Role enum을 String으로 변환
     */
    public static UserRequest toRequest(UserEntity user) {
        if (user == null) return null;
        return UserRequest.builder()
                .id(user.getId()) // UUID 직접 매핑
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null) // Role enum -> String
                .grade(user.getGrade())
                .build();
    }

    /**
     * UserEntity를 UserResponse로 변환합니다.
     * 
     * @param user 사용자 엔티티
     * @return 사용자 응답 DTO
     * 
     * 리팩토링 완료:
     * - UserEntity와 UserResponse 모두 UUID를 사용하므로 직접 매핑 가능
     * - Role enum을 String으로 변환
     */
    public static UserResponse toResponse(UserEntity user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId()) // UUID 직접 매핑
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null) // Role enum -> String
                .grade(user.getGrade())
                .build();
    }

    /**
     * RegisterRequest를 UserEntity로 변환합니다.
     * 
     * @param request 회원가입 요청
     * @param encodedPassword 암호화된 비밀번호
     * @return 사용자 엔티티
     * 
     * 리팩토링:
     * - UserEntity 사용
     * - passwordHash 필드명 사용
     * - Role enum 사용
     * 
     * 문제점:
     * - RegisterRequest는 String role을 사용하지만, UserEntity는 Role enum을 사용
     * - TODO: RegisterRequest의 role을 Role enum으로 변경하거나 변환 로직 추가 필요
     */
    public static UserEntity toEntity(RegisterRequest request, String encodedPassword) {
        if (request == null) return null;
        
        // String role을 Role enum으로 변환
        Role role = Role.USER; // 기본값
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 role 값이면 기본값 사용
                role = Role.USER;
            }
        }
        
        return UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(encodedPassword) // 리팩토링: passwordHash 필드명 사용
                .role(role) // 리팩토링: Role enum 사용
                .grade(Grade.BRONZE)
                .build();
    }
}
