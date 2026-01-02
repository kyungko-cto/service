package com.delivery.api.config.exception;

import com.delivery.api.ApiResponse;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import com.delivery.common.exception.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business logic error: {}", e.getErrorCode().getMessage());
        return ApiResponse.error(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<List<ValidationErrorResponse>>> handleValidation(MethodArgumentNotValidException e) {
        // stream 최적화 및 record 기반 응답
        List<ValidationErrorResponse> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ValidationErrorResponse(f.getField(), f.getDefaultMessage()))
                .toList();
        return ApiResponse.error(ErrorCode.INVALID_PARAM, "입력값이 잘못되었습니다.", errors);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Critical System Error", e); // StackTrace 로깅은 비동기로 처리됨
        return ApiResponse.error(ErrorCode.INTERNAL_ERROR);
    }
}