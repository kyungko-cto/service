package com.delivery.api.domain.order.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private UUID menuItemId;
    private int quantity;
}
