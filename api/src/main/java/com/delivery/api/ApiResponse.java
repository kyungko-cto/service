package com.delivery.api;

import com.delivery.common.exception.ErrorCode;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;

/**
 * 공통 API 응답 포맷
 *
 * 필드:
 * - success: 성공 여부
 * - code: 내부 에러 코드 또는 "SUCCESS"
 * - message: 사용자 표시용 메시지
 * - data: 실제 페이로드 (성공/에러 상세)
 * - timestamp: 응답 생성 시각 (디버깅/추적용)
 *
 * 설계 의도:
 * - 모든 컨트롤러/핸들러에서 일관된 응답 스키마를 제공.
 * - 정적 팩토리 메서드로 ResponseEntity 생성 로직을 중앙화.
 * - 타입 안정성을 위해 Void와 제네릭 타입을 명확히 구분.
 *
 * 리팩토링 사항:
 * 1. success() 메서드 분리: 데이터가 있는 경우와 없는 경우를 명확히 구분
 *    - 문제: 기존 success() 메서드가 제네릭 <T>를 사용하여 데이터가 없을 때 타입 추론이 어려움
 *    - 해결: success()를 두 개로 분리 (데이터 있음: <T>, 데이터 없음: Void 명시)
 * 2. error() 메서드 오버로딩 개선: Void 타입을 명시적으로 반환하는 메서드 추가
 *    - 문제: GlobalExceptionHandler에서 ApiResponse<Void>를 반환하는데, 제네릭 타입 추론이 필요함
 *    - 해결: Void를 명시적으로 반환하는 오버로드 메서드 추가로 타입 안정성 향상
 * 3. 메서드 시그니처 일관성: 모든 메서드가 명확한 타입을 반환하도록 개선
 *
 * 디버깅 포인트:
 * - now()의 타임존이 기대와 다르면 로그/모니터링에서 시간 불일치 발생 -> Clock 주입 고려.
 * - 핸들러에서 ApiResponse.error(...)를 바로 반환할 수 있도록 반환 타입 일치 확인.
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        OffsetDateTime timestamp
) {
    /**
     * 응답 생성 시각을 표준화합니다.
     * 
     * 리팩토링: private static 메서드로 캡슐화하여 응답 생성 시각의 일관성 보장
     * 향후 개선: Clock을 주입받아 테스트 가능하도록 개선 고려
     */
    private static OffsetDateTime now() {
        return OffsetDateTime.now();
    }

    // ========== 성공 응답 메서드 ==========

    /**
     * 성공 응답을 생성합니다 (데이터 포함).
     * 
     * @param data 응답 데이터 (null 가능)
     * @param <T> 응답 데이터 타입
     * @return ResponseEntity<ApiResponse<T>>
     * 
     * 리팩토링: 제네릭 타입을 명확히 하여 타입 안정성 향상
     * 사용 예: ApiResponse.success(orderResponse)
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        ApiResponse<T> body = new ApiResponse<>(true, "SUCCESS", "성공", data, now());
        return ResponseEntity.ok(body);
    }

    /**
     * 성공 응답을 생성합니다 (데이터 없음).
     * 
     * @return ResponseEntity<ApiResponse<Void>>
     * 
     * 리팩토링 사항 (실무 패턴 반영):
     * - 문제: 기존 success() 메서드가 제네릭 <T>를 사용하여 데이터가 없을 때 타입 추론이 어려움
     *   예: ApiResponse.success() 호출 시 컴파일러가 타입을 추론하지 못할 수 있음
     * - 해결: Void를 명시적으로 반환하는 별도 메서드 추가
     *   이렇게 하면 GlobalExceptionHandler에서 ResponseEntity<ApiResponse<Void>>를 반환할 때
     *   타입 추론이 명확해지고 컴파일 타임에 타입 안정성이 보장됨
     * 
     * 실무 사용 패턴:
     * - DELETE, PUT, PATCH 등 데이터를 반환하지 않는 작업에 사용
     * - 예: 사용자 삭제, 주문 취소, 상태 변경 등
     * - 클라이언트는 success 필드와 message로 작업 성공 여부 확인
     * - data는 null이지만, 일관된 응답 구조를 유지하여 클라이언트 처리 단순화
     * 
     * 사용 예: ApiResponse.success() - 데이터 없이 성공만 반환
     */
    public static ResponseEntity<ApiResponse<Void>> success() {
        ApiResponse<Void> body = new ApiResponse<>(true, "SUCCESS", "성공", null, now());
        return ResponseEntity.ok(body);
    }

    // ========== 에러 응답 메서드 (Void 타입) ==========

    /**
     * 에러 응답을 생성합니다 (ErrorCode 기반, 기본 메시지 사용, 데이터 없음).
     * 
     * @param errorCode 에러 코드
     * @return ResponseEntity<ApiResponse<Void>>
     * 
     * 리팩토링 사항:
     * - 문제: 기존 error() 메서드가 제네릭 <T>를 사용하여 Void 타입을 반환할 때 타입 추론이 필요함
     *   GlobalExceptionHandler에서 ResponseEntity<ApiResponse<Void>>를 반환하는데,
     *   제네릭 타입 추론이 명확하지 않아 컴파일러 경고가 발생할 수 있음
     * - 해결: Void를 명시적으로 반환하는 오버로드 메서드 추가
     *   이렇게 하면 타입 안정성이 향상되고 코드 가독성이 개선됨
     * 
     * 사용 예: ApiResponse.error(ErrorCode.USER_NOT_FOUND)
     */
    public static ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode) {
        ApiResponse<Void> body = new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null, now());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    /**
     * 에러 응답을 생성합니다 (ErrorCode + 커스텀 메시지, 데이터 없음).
     * 
     * @param errorCode 에러 코드
     * @param message 커스텀 메시지
     * @return ResponseEntity<ApiResponse<Void>>
     * 
     * 리팩토링: Void 타입을 명시적으로 반환하여 타입 안정성 향상
     * 사용 예: ApiResponse.error(ErrorCode.INVALID_PARAM, "요청 바디를 읽을 수 없습니다.")
     */
    public static ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode, String message) {
        ApiResponse<Void> body = new ApiResponse<>(false, errorCode.getCode(), message, null, now());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    // ========== 에러 응답 메서드 (제네릭 타입 - Validation Errors 등) ==========

    /**
     * 에러 응답을 생성합니다 (ErrorCode + 데이터, 기본 메시지 사용).
     * 
     * @param errorCode 에러 코드
     * @param data 에러 상세 데이터 (예: ValidationErrorResponse 리스트)
     * @param <T> 에러 데이터 타입
     * @return ResponseEntity<ApiResponse<T>>
     * 
     * 리팩토링 사항:
     * - 문제: 기존 error() 메서드가 모든 경우에 제네릭을 사용하여 타입 추론이 복잡함
     * - 해결: 데이터가 있는 경우와 없는 경우를 명확히 구분
     *   데이터가 있는 경우(예: validation errors)는 제네릭 타입을 사용하고,
     *   데이터가 없는 경우는 Void를 명시적으로 반환하는 메서드를 사용
     * 
     * 사용 예: ApiResponse.error(ErrorCode.INVALID_PARAM, validationErrors)
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, T data) {
        ApiResponse<T> body = new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), data, now());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    /**
     * 에러 응답을 생성합니다 (ErrorCode + 커스텀 메시지 + 데이터).
     * 
     * @param errorCode 에러 코드
     * @param message 커스텀 메시지
     * @param data 에러 상세 데이터 (예: ValidationErrorResponse 리스트)
     * @param <T> 에러 데이터 타입
     * @return ResponseEntity<ApiResponse<T>>
     * 
     * 리팩토링: 제네릭 타입을 명확히 하여 타입 안정성 향상
     * 사용 예: ApiResponse.error(ErrorCode.INVALID_PARAM, "입력값이 잘못되었습니다.", validationErrors)
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode errorCode, String message, T data) {
        ApiResponse<T> body = new ApiResponse<>(false, errorCode.getCode(), message, data, now());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }
}
