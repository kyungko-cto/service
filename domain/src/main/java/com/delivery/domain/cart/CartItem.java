package com.delivery.domain.cart;


import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CartItem {
    private UUID menuItemId;
    private String menuItemName;
    private int unitPrice;
    private int quantity;

    public int getLineAmount() {
        return unitPrice * quantity;
    }
}

