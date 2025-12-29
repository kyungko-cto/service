package com.delivery.api.domain.delivery.converter;


import com.delivery.api.domain.delivery.dto.DeliveryRequest;
import com.delivery.api.domain.delivery.dto.DeliveryResponse;
import com.delivery.domain.delivery.Delivery;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DeliveryConverter {

    /**
     * 요청 DTO → 도메인 모델
     */
    public Delivery toDomain(DeliveryRequest request) {
        if (request == null) return null;
        return Delivery.builder()
                .orderId(request.getOrderId())
                .riderName(request.getRiderName())
                .destinationAddressId(request.getDestinationAddressId())
                .build();
    }

    /**
     * 도메인 모델 → 응답 DTO
     */
    public DeliveryResponse toResponse(Delivery delivery) {
        if (delivery == null) return null;
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
