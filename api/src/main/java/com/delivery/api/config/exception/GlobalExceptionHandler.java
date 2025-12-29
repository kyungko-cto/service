package com.delivery.api.config.exception;

import com.delivery.api.ApiResponse;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.delivery.api.ApiResponse.error;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직 예외
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException at {}: {}", e.getStackTrace()[0], e.getErrorCode().getMessage());
        return error(e.getErrorCode());
    }

    // @Valid 유효성 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Validation failed in method: {}", e.getParameter().getExecutable().toGenericString(), e);

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        return ApiResponse.<Map<String, String>>error(ErrorCode.INVALID_PARAM, "입력값이 잘못되었습니다.", errors);
        //map써서 하지말고 클래스를 하나 만들자
    }

    // 모든 예외 처리 (500)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected Exception at {}: {}", e.getStackTrace()[0], e.getMessage(), e);
        return error(ErrorCode.INTERNAL_ERROR);
    }
}
