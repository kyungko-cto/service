package com.delivery.service.delivery;


import com.delivery.domain.delivery.Delivery;
import com.delivery.domain.delivery.DeliveryStatus;
import com.delivery.db.entity.delivery.DeliveryEntity;
import com.delivery.db.entity.delivery.DeliveryRepository;
import com.delivery.common.exception.BusinessException;
import com.delivery.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public UUID assign(UUID orderId, String riderName, UUID destinationAddressId) {
        UUID deliveryId = UUID.randomUUID();
        DeliveryEntity entity = new DeliveryEntity();
        entity.setId(deliveryId);
        entity.setOrderId(orderId);
        entity.setRiderName(riderName);
        entity.setDestinationAddressId(destinationAddressId);
        entity.setStatus(DeliveryStatus.ASSIGNED.name());
        entity.setAssignedAt(OffsetDateTime.now());
        deliveryRepository.save(entity);
        return deliveryId;
    }

    public Optional<Delivery> getById(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId).map(this::toDomain);
    }

    public void pickUp(UUID deliveryId) {
        DeliveryEntity entity = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
        entity.setStatus(DeliveryStatus.PICKED_UP.name());
        entity.setPickedUpAt(OffsetDateTime.now());
        deliveryRepository.save(entity);
    }

    public void complete(UUID deliveryId) {
        DeliveryEntity entity = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
        entity.setStatus(DeliveryStatus.COMPLETED.name());
        entity.setCompletedAt(OffsetDateTime.now());
        deliveryRepository.save(entity);
    }

    private Delivery toDomain(DeliveryEntity entity) {
        return Delivery.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .riderName(entity.getRiderName())
                .destinationAddressId(entity.getDestinationAddressId())
                .status(DeliveryStatus.valueOf(entity.getStatus()))
                .assignedAt(entity.getAssignedAt())
                .pickedUpAt(entity.getPickedUpAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
