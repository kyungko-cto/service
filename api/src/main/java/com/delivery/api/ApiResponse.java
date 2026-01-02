package com.delivery.api;

import com.delivery.common.exception.ErrorCode;
import org.springframework.http.ResponseEntity;

// record를 사용하여 메모리 오버헤드 감소
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, "SUCCESS", "성공", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, String message, T data) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(false, errorCode.getCode(), message, data));
    }
}