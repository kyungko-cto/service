package com.delivery.api.domain.delivery.dto;


import com.delivery.domain.delivery.DeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class DeliveryResponse {
    private UUID deliveryId;
    private UUID orderId;
    private String riderName;
    private DeliveryStatus status;
    private UUID destinationAddressId;
    private OffsetDateTime assignedAt;
    private OffsetDateTime pickedUpAt;
    private OffsetDateTime completedAt;
}
