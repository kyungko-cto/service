package com.delivery.domain.store;


import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Store {
    private UUID id;
    private String name;
    private String address;
    private StoreStatus status;
    private String phone;

    public void rename(String name) {
        this.name = name;
    }

    public void relocate(String address) {
        this.address = address;
    }

    public void changePhone(String phone) {
        this.phone = phone;
    }

    public void open() {
        this.status = StoreStatus.OPEN;
    }

    public void close() {
        this.status = StoreStatus.CLOSED;
    }

    public void suspend() {
        this.status = StoreStatus.SUSPENDED;
    }

    public boolean isOpen() {
        return this.status == StoreStatus.OPEN;
    }
}

