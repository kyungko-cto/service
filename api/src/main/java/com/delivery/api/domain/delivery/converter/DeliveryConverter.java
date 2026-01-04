package com.delivery.api.domain.delivery.converter;

import com.delivery.api.domain.delivery.dto.DeliveryRequest;
import com.delivery.api.domain.delivery.dto.DeliveryResponse;
import com.delivery.domain.delivery.Delivery;
import lombok.experimental.UtilityClass;

/**
 * 배달 관련 변환기
 * 
 * 리팩토링: DTO와 도메인 객체 간 변환 로직
 */
@UtilityClass
public class DeliveryConverter {

    /**
     * 요청 DTO → 도메인 모델
     * 
     * @param request 배달 요청 DTO
     * @return 배달 도메인 객체
     * 
     * 리팩토링: null 체크 및 주석 추가
     */
    public Delivery toDomain(DeliveryRequest request) {
        if (request == null) {
            return null;
        }
        return Delivery.builder()
                .orderId(request.getOrderId())
                .riderName(request.getRiderName())
                .destinationAddressId(request.getDestinationAddressId())
                .build();
    }

    /**
     * 도메인 모델 → 응답 DTO
     * 
     * @param delivery 배달 도메인 객체
     * @return 배달 응답 DTO
     * 
     * 리팩토링: null 체크 및 주석 추가
     */
    public DeliveryResponse toResponse(Delivery delivery) {
        if (delivery == null) {
            return null;
        }
        return DeliveryResponse.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .riderName(delivery.getRiderName())
                .status(delivery.getStatus())
                .destinationAddressId(delivery.getDestinationAddressId())
                .assignedAt(delivery.getAssignedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .completedAt(delivery.getCompletedAt())
                .build();
    }
}
