package com.delivery.api.config.exception;

import com.delivery.api.ApiResponse;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import com.delivery.common.exception.JwtException;
import com.delivery.common.exception.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException; // Spring Boot 3+ 환경에서는 jakarta 사용

import java.util.List;

/**
 * 전역 예외 처리기
 *
 * 설계 의도:
 * - 컨트롤러 전역에서 발생하는 예외를 중앙에서 처리해 일관된 ApiResponse 반환.
 * - 예외 타입별로 적절한 로그 레벨과 응답을 분리.
 *
 * 디버깅 포인트:
 * - ConstraintViolationException import가 빨간불이면 Spring Boot 버전 확인 (jakarta vs javax).
 * - 특정 예외가 의도한 핸들러에 도달하지 않으면 예외 상속 구조 확인.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리: 클라이언트 오류로 간주, warn 레벨로 로깅
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        log.warn("Business error: {} - {}", code.getCode(), e.getMessage()); // 운영에서 필터링하기 쉬운 warn
        return ApiResponse.error(code, e.getMessage()); // 에러 코드와 커스텀 메시지 반환
    }

    // JWT/인증 예외 처리: 인증 실패는 UNAUTHORIZED로 응답
    @ExceptionHandler(JwtException.class)
    protected ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException e) {
        ErrorCode code = e.getErrorCode();
        log.warn("Authentication error: {} - {}. cause: {}", code.getCode(), e.getMessage(), e.getCause());//cause가 메시지랑 에러코드에 담긴거 아닐까?
        return ApiResponse.error(code, e.getMessage());
    }

    // 권한 없음(인가) 처리: AccessDeniedException -> FORBIDDEN 토큰에 관한?
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.ACCESS_DENIED);
    }

    // 요청 바디 파싱 실패 (malformed JSON 등) 이거는 리퀘스트바디에대한 읽을수없는?
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Malformed request body: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.INVALID_PARAM, "요청 바디를 읽을 수 없습니다.");
    }

    // @Valid 바인딩 실패 -> 필드별 상세 응답 형식이 틀린?
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<List<ValidationErrorResponse>>> handleValidation(MethodArgumentNotValidException e) {
        List<ValidationErrorResponse> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ValidationErrorResponse(f.getField(), f.getDefaultMessage()))
                .toList();
        log.warn("Validation failed: {} errors", errors.size());
        return ApiResponse.error(ErrorCode.INVALID_PARAM, "입력값이 잘못되었습니다.", errors);
    }

    // @Validated 파라미터 제약 위반 처리 (메서드 파라미터 검증)
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiResponse<List<ValidationErrorResponse>>> handleConstraintViolation(ConstraintViolationException e) {
        List<ValidationErrorResponse> errors = e.getConstraintViolations().stream()
                .map(v -> new ValidationErrorResponse(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        log.warn("Constraint violations: {} errors", errors.size());
        return ApiResponse.error(ErrorCode.INVALID_PARAM, "입력값 제약 조건을 만족하지 않습니다.", errors);
    }

    // 최종 안전망: 예기치 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception occurred", e); // 스택트레이스 포함 로깅
        return ApiResponse.error(ErrorCode.INTERNAL_ERROR);
    }
}
