package com.delivery.api.domain.payment.dto;

import com.delivery.domain.payment.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 결제 응답 DTO
 * 
 * 리팩토링: 결제 정보 응답 구조
 */
@Getter
@Builder
public class PaymentResponse {
    private UUID paymentId;
    private UUID orderId;
    private int amount;
    private PaymentStatus status;
    private String provider;
    private String transactionId;
}

