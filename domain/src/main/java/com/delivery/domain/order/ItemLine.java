package com.delivery.domain.order;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ItemLine {
    private UUID menuItemId;
    private String menuItemName;
    private int unitPrice;
    private int quantity;

    public int getLineAmount() {
        return unitPrice * quantity;
    }
}
