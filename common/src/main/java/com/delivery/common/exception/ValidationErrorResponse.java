package com.delivery.common.exception;

/**
 * 필드 검증 에러 응답 DTO
 *
 * 설계 의도:
 * - 검증 실패 시 프론트가 어떤 필드에서 어떤 문제가 발생했는지 쉽게 파싱하도록 제공.
 * - record로 선언해 불변성과 간결성 확보.
 *
 * 디버깅 포인트:
 * - Jackson 직렬화 문제 발생 시 Spring Boot와 Jackson 버전 호환성 확인.
 */
public record ValidationErrorResponse(
        String field,
        String message
) {}
