package com.delivery.api.domain.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 결제 요청 DTO
 * 
 * 리팩토링: 유효성 검증 어노테이션 추가
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    @NotNull(message = "주문 ID는 필수입니다")
    private UUID orderId;
    
    @Min(value = 1, message = "결제 금액은 1 이상이어야 합니다")
    private int amount;
    
    @NotBlank(message = "결제 수단은 필수입니다")
    private String provider;
}

