package com.delivery.common.exception;

/**
 * JWT 검증 실패 예외
 *
 * 설계 의도:
 * - 인증 관련 오류를 명확히 표현하고 ErrorCode를 함께 보관해 전역 핸들러에서 일관된 응답을 생성.
 * - RuntimeException 상속으로 호출부에서 강제 처리하지 않도록 함.
 *
 * 디버깅 포인트:
 * - errorCode가 null이면 getHttpStatus() 호출 시 NPE 발생 -> 생성자에서 기본값 보장 확인.
 * - 예외를 던질 때 cause를 전달하지 않으면 스택트레이스 추적이 어려움.
 */
public class JwtException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    // 예외에 매핑된 ErrorCode를 보관
    private final ErrorCode errorCode;

    // 기본 생성자: 메시지만 전달하면 UNAUTHORIZED로 매핑
    public JwtException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNAUTHORIZED;
    }

    // 특정 ErrorCode를 명시적으로 전달 가능
    public JwtException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : ErrorCode.UNAUTHORIZED;
    }

    // 원인(cause)을 포함해 던질 때 사용
    public JwtException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.UNAUTHORIZED;
    }

    // message, errorCode, cause 모두 지정 가능
    public JwtException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : ErrorCode.UNAUTHORIZED;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    // 전역 핸들러에서 상태를 바로 얻기 위한 편의 메서드
    public org.springframework.http.HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
