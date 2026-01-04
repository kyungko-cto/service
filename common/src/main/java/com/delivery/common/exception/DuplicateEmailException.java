package com.delivery.common.exception;


/**
 * 중복된 이메일로 회원가입 시도 예외
 */
public class DuplicateEmailException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "이미 가입된 이메일입니다";

    public DuplicateEmailException() {
        super(DEFAULT_MESSAGE);
    }

    public DuplicateEmailException(String message) {
        super(message);
    }
}