package com.delivery.common.exception;

/**
 * 잘못된 자격 증명 (이메일 또는 비밀번호 오류)
 */
public class InvalidCredentialsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "이메일 또는 비밀번호가 틀렸습니다";

    public InvalidCredentialsException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}