package com.delivery.api.domain.cart.dto;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RemoveItemRequest {
    private UUID menuItemId;
}
