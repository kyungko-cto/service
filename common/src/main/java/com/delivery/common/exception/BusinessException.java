package com.delivery.common.exception;

/**
 * 도메인 비즈니스 예외
 *
 * 설계 의도:
 * - 서비스 레이어에서 비즈니스 규칙 위반 시 사용.
 * - ErrorCode로 매핑해 API 응답을 표준화.
 *
 * 디버깅 포인트:
 * - 예외를 던지는 지점에서 어떤 ErrorCode를 사용하는지 코드 리뷰 필요.
 * - 전역 핸들러에서 BusinessException을 잡지 못하면 핸들러 스캔 범위 확인.
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    // ErrorCode만 전달하면 기본 메시지를 사용
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 메시지를 오버라이드할 수 있음
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : ErrorCode.INTERNAL_ERROR;
    }

    // 원인 포함 생성자
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : ErrorCode.INTERNAL_ERROR;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
