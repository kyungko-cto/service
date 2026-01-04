package com.delivery.api.domain.payment;

import com.delivery.api.ApiResponse;
import com.delivery.api.domain.payment.converter.PaymentConverter;
import com.delivery.api.domain.payment.dto.PaymentRequest;
import com.delivery.api.domain.payment.dto.PaymentResponse;
import com.delivery.service.payment.PaymentService;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 결제 컨트롤러
 * 
 * 리팩토링 사항:
 * 1. 잘못된 import 경로 수정
 * 2. 유효성 검증 추가
 * 3. 주석 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제를 요청합니다.
     * 
     * @param request 결제 요청 정보
     * @return 결제 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> request(
            @Valid @RequestBody PaymentRequest request) {
        UUID paymentId = paymentService.request(
                request.getOrderId(),
                request.getAmount(),
                request.getProvider()
        );
        var payment = paymentService.getById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED, "결제를 찾을 수 없습니다"));
        return ApiResponse.success(PaymentConverter.toResponse(payment));
    }

    /**
     * 결제를 성공 상태로 변경합니다.
     * 
     * @param paymentId 결제 ID
     * @param transactionId 거래 ID
     * @return 결제 정보
     */
    @PostMapping("/{paymentId}/success")
    public ResponseEntity<ApiResponse<PaymentResponse>> success(
            @PathVariable UUID paymentId,
            @RequestParam String transactionId) {
        paymentService.markSuccess(paymentId, transactionId);
        var payment = paymentService.getById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED, "결제를 찾을 수 없습니다"));
        return ApiResponse.success(PaymentConverter.toResponse(payment));
    }

    /**
     * 결제를 조회합니다.
     * 
     * @param paymentId 결제 ID
     * @return 결제 정보
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> get(@PathVariable UUID paymentId) {
        var payment = paymentService.getById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED, "결제를 찾을 수 없습니다"));
        return ApiResponse.success(PaymentConverter.toResponse(payment));
    }
}

