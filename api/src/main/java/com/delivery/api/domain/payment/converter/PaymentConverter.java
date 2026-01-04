package com.delivery.api.domain.payment.converter;

import com.delivery.api.domain.payment.dto.PaymentRequest;
import com.delivery.api.domain.payment.dto.PaymentResponse;
import com.delivery.domain.payment.Payment;
import lombok.experimental.UtilityClass;

/**
 * 결제 관련 변환기
 * 
 * 리팩토링: DTO와 도메인 객체 간 변환 로직
 */
@UtilityClass
public class PaymentConverter {

    /**
     * 요청 DTO → 도메인 모델
     * 
     * @param request 결제 요청 DTO
     * @return 결제 도메인 객체
     */
    public Payment toDomain(PaymentRequest request) {
        if (request == null) {
            return null;
        }
        // PaymentRequest는 단순히 orderId, amount, provider만 포함
        // 실제 Payment 도메인 객체는 서비스에서 생성
        return null; // 서비스에서 직접 생성
    }

    /**
     * 도메인 모델 → 응답 DTO
     * 
     * @param payment 결제 도메인 객체
     * @return 결제 응답 DTO
     */
    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .provider(payment.getProvider())
                .transactionId(payment.getTransactionId())
                .build();
    }
}

