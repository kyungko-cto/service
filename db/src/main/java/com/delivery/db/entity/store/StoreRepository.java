package com.delivery.db.entity.store;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<StoreEntity, UUID> {
    List<StoreEntity> findByOwnerId(UUID ownerId);
    List<StoreEntity> findByStatus(StoreStatus status);
    Optional<StoreEntity> findByName(String name);
}
