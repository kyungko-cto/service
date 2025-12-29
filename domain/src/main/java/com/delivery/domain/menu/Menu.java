package com.delivery.domain.menu;


import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Menu {
    private UUID id;
    private UUID storeId;
    private String name;
    private String description;
    private int price;
    private MenuCategory category;
    private MenuStatus status;

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePrice(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price must be non-negative.");
        }
        this.price = price;
    }

    public void changeCategory(MenuCategory category) {
        this.category = category;
    }

    public void markAvailable() {
        this.status = MenuStatus.AVAILABLE;
    }

    public void markUnavailable() {
        this.status = MenuStatus.UNAVAILABLE;
    }

    public boolean isAvailable() {
        return this.status == MenuStatus.AVAILABLE;
    }
}
