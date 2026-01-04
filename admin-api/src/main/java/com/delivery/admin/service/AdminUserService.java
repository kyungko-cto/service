package com.delivery.admin.service;

import com.delivery.admin.dto.UserListResponse;
import com.delivery.admin.dto.UserDetailResponse;
import com.delivery.db.entity.user.UserEntity;
import com.delivery.db.entity.user.UserRepository;
import com.delivery.db.entity.user.Grade;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 관리자 사용자 관리 서비스
 * 
 * 리팩토링 사항:
 * 1. 예외 처리 개선: IllegalStateException을 BusinessException으로 변환
 * 2. 로깅 추가: 관리자 작업 추적을 위한 로그
 * 3. DTO 변환 로직 개선: null 안전성 강화
 * 4. 주석 추가: 각 메서드의 목적과 비즈니스 규칙 설명
 * 
 * 설계 원칙:
 * - application 모듈의 서비스를 재사용하지 않고 독립적으로 구현
 *   이유: 관리자 기능은 일반 사용자 기능과 분리하여 보안과 권한 관리 용이
 * - 트랜잭션 보장: 모든 상태 변경 작업에 @Transactional 적용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    /**
     * 사용자 목록 조회 (페이징)
     * 
     * @param pageable 페이징 정보
     * @return 사용자 목록 (페이징)
     * 
     * 리팩토링: 간단한 조회 작업이므로 트랜잭션 불필요
     */
    public Page<UserListResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toListResponse);
    }

    /**
     * 사용자 상세 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 상세 정보
     * @throws BusinessException 사용자를 찾을 수 없을 때
     */
    public UserDetailResponse getUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return toDetailResponse(user);
    }

    /**
     * 사용자 계정 활성화
     * 
     * @param userId 사용자 ID
     * @throws BusinessException 사용자를 찾을 수 없거나 활성화할 수 없는 상태일 때
     * 
     * 리팩토링 사항:
     * - 문제: UserEntity.activate()가 IllegalStateException을 던지는데, 이를 BusinessException으로 변환 필요
     * - 해결: try-catch로 예외를 잡아 BusinessException으로 변환
     * - 로깅: 관리자 작업 추적을 위한 로그 추가
     */
    @Transactional
    public void activateUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        try {
            user.activate();
            userRepository.save(user);
            log.info("관리자 작업: 사용자 계정 활성화 - userId: {}", userId);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "계정을 활성화할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 계정 비활성화 (일시 중지)
     * 
     * @param userId 사용자 ID
     * @throws BusinessException 사용자를 찾을 수 없거나 중지할 수 없는 상태일 때
     * 
     * 리팩토링 사항:
     * - 문제: UserEntity.suspend()가 IllegalStateException을 던지는데, 이를 BusinessException으로 변환 필요
     * - 해결: try-catch로 예외를 잡아 BusinessException으로 변환
     * - 로깅: 관리자 작업 추적을 위한 로그 추가
     */
    @Transactional
    public void suspendUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        try {
            user.suspend();
            userRepository.save(user);
            log.info("관리자 작업: 사용자 계정 일시 중지 - userId: {}", userId);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "계정을 중지할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 등급 변경
     * 
     * @param userId 사용자 ID
     * @param gradeStr 등급 문자열 (BRONZE, SILVER, GOLD)
     * @throws BusinessException 사용자를 찾을 수 없거나 유효하지 않은 등급일 때
     * 
     * 리팩토링 사항:
     * - 등급 문자열 검증 개선
     * - 로깅 추가
     */
    @Transactional
    public void changeGrade(UUID userId, String gradeStr) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        if (gradeStr == null || gradeStr.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "등급은 필수입니다");
        }
        
        Grade grade;
        try {
            grade = Grade.valueOf(gradeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, 
                    "유효하지 않은 등급입니다: " + gradeStr + " (가능한 값: BRONZE, SILVER, GOLD)");
        }
        
        user.upgradeGrade(grade);
        userRepository.save(user);
        log.info("관리자 작업: 사용자 등급 변경 - userId: {}, 등급: {}", userId, grade);
    }

    /**
     * UserEntity를 UserListResponse로 변환합니다.
     * 
     * @param user 사용자 엔티티
     * @return 사용자 목록 응답 DTO
     * 
     * 리팩토링: null 안전성 강화 (enum이 null일 수 있는 경우 대비)
     */
    private UserListResponse toListResponse(UserEntity user) {
        return UserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .grade(user.getGrade() != null ? user.getGrade().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * UserEntity를 UserDetailResponse로 변환합니다.
     * 
     * @param user 사용자 엔티티
     * @return 사용자 상세 응답 DTO
     * 
     * 리팩토링: null 안전성 강화 (enum이 null일 수 있는 경우 대비)
     */
    private UserDetailResponse toDetailResponse(UserEntity user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .grade(user.getGrade() != null ? user.getGrade().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

