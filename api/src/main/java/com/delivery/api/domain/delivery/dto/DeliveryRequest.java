package com.delivery.api.domain.delivery.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {
    private UUID orderId;
    private String riderName;
    private UUID destinationAddressId;
}

