package com.delivery.api.domain.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 배달 배정 요청 DTO
 * 
 * 리팩토링: 유효성 검증 어노테이션 추가
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {
    @NotNull(message = "주문 ID는 필수입니다")
    private UUID orderId;
    
    @NotBlank(message = "배달원 이름은 필수입니다")
    private String riderName;
    
    @NotNull(message = "배송지 주소 ID는 필수입니다")
    private UUID destinationAddressId;
}

