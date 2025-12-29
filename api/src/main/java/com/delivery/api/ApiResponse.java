package com.delivery.api;

import com.delivery.common.exception.ErrorCode;
import org.springframework.http.ResponseEntity;

public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공 응답
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, "SUCCESS", "성공", data));
    }

    // 에러 응답 (ErrorCode 기반)
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null));
    }

    // 에러 응답 (커스텀 메시지)
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, String customMessage) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(false, errorCode.getCode(), customMessage, null));
    }

    // 에러 응답 (추가 데이터 포함)
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, String customMessage, T data) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(false, errorCode.getCode(), customMessage, data));
    }

    // Getter
    public boolean isSuccess() { return success; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
