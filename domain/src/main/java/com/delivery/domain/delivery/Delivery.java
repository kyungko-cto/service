package com.delivery.domain.delivery;


import com.delivery.domain.delivery.DeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class Delivery {
    private UUID id;
    private UUID orderId;
    private String riderName;
    private DeliveryStatus status;
    private UUID destinationAddressId;
    private OffsetDateTime assignedAt;
    private OffsetDateTime pickedUpAt;
    private OffsetDateTime completedAt;

    public void assign(String riderName, UUID destinationAddressId) {
        this.riderName = riderName;
        this.destinationAddressId = destinationAddressId;
        this.status = DeliveryStatus.ASSIGNED;
        this.assignedAt = OffsetDateTime.now();
    }

    public void pickUp() {
        this.status = DeliveryStatus.PICKED_UP;
        this.pickedUpAt = OffsetDateTime.now();
    }

    public void complete() {
        this.status = DeliveryStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
    }
}
