package com.delivery.common.exception;

/**
 * 인증 필요 예외 (토큰 없음 또는 유효하지 않음)
 */
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "인증이 필요합니다";

    public UnauthorizedException() {
        super(DEFAULT_MESSAGE);
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}