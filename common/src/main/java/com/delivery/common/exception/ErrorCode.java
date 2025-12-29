package com.delivery.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // ==========================
    // 시스템 공통 에러
    // ==========================
    INVALID_PARAM("COMMON_001", "잘못된 파라미터입니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("COMMON_002", "입력값 유효성 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("COMMON_003", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERNAL_ERROR("COMMON_004", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCESS_DENIED("COMMON_005", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // ==========================
    // 사용자 관련 에러
    // ==========================
    USER_ID_MISMATCH("USER_001", "사용자 아이디가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    DUPLICATE_EMAIL("USER_002", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("USER_003", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("USER_004", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_SUSPENDED("USER_005", "정지된 사용자입니다.", HttpStatus.FORBIDDEN),

    // ==========================
    // 매장 관련 에러
    // ==========================
    STORE_NOT_FOUND("STORE_001", "매장을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    STORE_CLOSED("STORE_002", "매장이 현재 영업 중이 아닙니다.", HttpStatus.BAD_REQUEST),
    STORE_ACCESS_DENIED("STORE_003", "해당 매장에 접근할 수 없습니다.", HttpStatus.FORBIDDEN),

    // ==========================
    // 메뉴 관련 에러
    // ==========================
    MENU_NOT_FOUND("MENU_001", "메뉴를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MENU_UNAVAILABLE("MENU_002", "해당 메뉴는 현재 주문할 수 없습니다.", HttpStatus.BAD_REQUEST),
    STOCK_EMPTY("MENU_003", "메뉴 재고가 부족합니다.", HttpStatus.CONFLICT),
    DUPLICATE_MENU("MENU_004", "이미 존재하는 메뉴입니다.", HttpStatus.CONFLICT),

    // ==========================
    // 주문 관련 에러
    // ==========================
    ORDER_NOT_FOUND("ORDER_001", "주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_COMPLETED("ORDER_002", "이미 완료된 주문입니다.", HttpStatus.CONFLICT),
    ORDER_CANNOT_CANCEL("ORDER_003", "현재 상태에서는 주문을 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),
    ORDER_NUMBER_INVALID("ORDER_004", "주문 번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // ==========================
    // 결제 관련 에러
    // ==========================
    PAYMENT_FAILED("PAYMENT_001", "결제에 실패했습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PROCESSED("PAYMENT_002", "이미 처리된 결제입니다.", HttpStatus.CONFLICT),
    PAYMENT_METHOD_UNSUPPORTED("PAYMENT_003", "지원하지 않는 결제 수단입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH("PAYMENT_004", "결제 금액이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // ==========================
    // 배달 관련 에러
    // ==========================
    DELIVERY_NOT_FOUND("DELIVERY_001", "배달 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELIVERY_ALREADY_COMPLETED("DELIVERY_002", "이미 완료된 배달입니다.", HttpStatus.CONFLICT),
    DELIVERY_ASSIGN_FAILED("DELIVERY_003", "배달원을 배정할 수 없습니다.", HttpStatus.BAD_REQUEST),
    DELIVERY_STATUS_INVALID("DELIVERY_004", "배달 상태가 올바르지 않습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
