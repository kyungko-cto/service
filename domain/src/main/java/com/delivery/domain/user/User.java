package com.delivery.domain.user;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 사용자 도메인 엔티티
 * 
 * 리팩토링 사항:
 * 1. 불필요한 import 제거: 같은 패키지의 클래스는 import 불필요
 * 2. Role enum 값 수정: ROLE_ADMIN -> ADMIN, ROLE_USER -> USER (실제 enum 값과 일치)
 * 3. promoteGrade/demoteGrade 메서드 개선: null 체크 추가 및 더 명확한 로직
 * 4. 메서드 순서 정리: 필드 접근 메서드 -> 상태 변경 메서드 -> 비즈니스 로직 메서드 순으로 재배치
 * 5. 주석 추가: 각 메서드의 목적과 동작 설명
 */
@Getter
@Builder
public class User {
    private UUID id;
    private String email;
    private String name;
    private Role role;
    private Grade grade;
    private boolean active;

    // ========== 상태 조회 메서드 ==========
    
    /**
     * 관리자 권한 여부를 확인합니다.
     * @return 관리자 권한이 있으면 true
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * 활성화 상태 여부를 확인합니다.
     * @return 활성화되어 있으면 true
     */
    public boolean isActive() {
        return this.active;
    }

    // ========== 기본 정보 변경 메서드 ==========
    
    /**
     * 사용자 이름을 변경합니다.
     * @param name 새로운 이름 (null이거나 빈 문자열일 수 있음 - 비즈니스 규칙에 따라 검증 필요)
     */
    public void rename(String name) {
        this.name = name;
    }

    /**
     * 이메일 주소를 변경합니다.
     * @param email 새로운 이메일 주소 (null이거나 빈 문자열일 수 있음 - 비즈니스 규칙에 따라 검증 필요)
     */
    public void changeEmail(String email) {
        this.email = email;
    }

    // ========== 등급 변경 메서드 ==========
    
    /**
     * 사용자 등급을 한 단계 상승시킵니다.
     * BRONZE -> SILVER -> GOLD 순서로 상승하며, 이미 GOLD 등급이면 변경하지 않습니다.
     * 
     * 리팩토링: null 체크 추가 및 더 명확한 로직 구조
     */
    public void promoteGrade() {
        if (this.grade == null) {
            return; // 등급이 설정되지 않은 경우 처리하지 않음
        }
        
        switch (this.grade) {
            case BRONZE -> this.grade = Grade.SILVER;
            case SILVER -> this.grade = Grade.GOLD;
            case GOLD -> {
                // 이미 최고 등급이므로 변경하지 않음
            }
        }
    }

    /**
     * 사용자 등급을 한 단계 하락시킵니다.
     * GOLD -> SILVER -> BRONZE 순서로 하락하며, 이미 BRONZE 등급이면 변경하지 않습니다.
     * 
     * 리팩토링: null 체크 추가 및 더 명확한 로직 구조
     */
    public void demoteGrade() {
        if (this.grade == null) {
            return; // 등급이 설정되지 않은 경우 처리하지 않음
        }
        
        switch (this.grade) {
            case GOLD -> this.grade = Grade.SILVER;
            case SILVER -> this.grade = Grade.BRONZE;
            case BRONZE -> {
                // 이미 최저 등급이므로 변경하지 않음
            }
        }
    }

    // ========== 역할 변경 메서드 ==========
    
    /**
     * 사용자에게 관리자 권한을 부여합니다.
     * 
     * 리팩토링: Role enum의 실제 값(ADMIN)을 사용하도록 수정
     */
    public void grantAdmin() {
        this.role = Role.ADMIN;
    }

    /**
     * 사용자에게 일반 사용자 권한을 부여합니다.
     * 
     * 리팩토링: Role enum의 실제 값(USER)을 사용하도록 수정
     */
    public void grantUser() {
        this.role = Role.USER;
    }

    // ========== 활성화 상태 변경 메서드 ==========
    
    /**
     * 사용자 계정을 활성화합니다.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * 사용자 계정을 비활성화합니다.
     */
    public void deactivate() {
        this.active = false;
    }
}
