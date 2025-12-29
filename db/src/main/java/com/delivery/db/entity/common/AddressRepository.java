package com.delivery.db.entity.common;


import com.delivery.db.entity.store.StoreEntity;
import com.delivery.db.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
    List<AddressEntity> findByUser(UserEntity user);
    List<AddressEntity> findByStore(StoreEntity store);
    List<AddressEntity> findByCity(String city);
    List<AddressEntity> findByDistrict(String district);
    List<AddressEntity> findByPostalCode(String postalCode);
}
