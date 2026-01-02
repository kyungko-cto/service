package com.delivery.domain.cart;


import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Cart {//redis를 써서 하는건데
    private UUID userId;
    private UUID storeId;
    private int totalAmount;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public void addItem(CartItem item) {
        this.items.add(item);
        recalcTotal();
    }

    public void removeItem(UUID menuItemId) {
        this.items.removeIf(i -> i.getMenuItemId().equals(menuItemId));
        recalcTotal();
    }

    public void clear() {
        this.items.clear();
        this.totalAmount = 0;
    }

    private void recalcTotal() {
        this.totalAmount = items.stream()
                .mapToInt(CartItem::getLineAmount)
                .sum();
    }
}
