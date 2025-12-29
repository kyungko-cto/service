package com.delivery.domain.address;


import com.delivery.db.entity.common.AddressEntity;
import com.delivery.domain.address.model.Address;

public class AddressConverter {

    public static Address toDomain(AddressEntity e) {
        return Address.builder()
                .id(e.getId())
                .city(e.getCity())
                .district(e.getDistrict())
                .street(e.getStreet())
                .detail(e.getDetail())
                .postalCode(e.getPostalCode())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .storeId(e.getStore() != null ? e.getStore().getId() : null)
                .build();
    }

    public static AddressEntity toEntity(Address d) {
        return AddressEntity.builder()
                .id(d.getId())
                .city(d.getCity())
                .district(d.getDistrict())
                .street(d.getStreet())
                .detail(d.getDetail())
                .postalCode(d.getPostalCode())
                .build();
    }
}
