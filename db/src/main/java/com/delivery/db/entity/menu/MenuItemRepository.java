package com.delivery.db.entity.menu;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItemEntity, UUID> {
    List<MenuItemEntity> findByStoreIdOrderByNameAsc(UUID storeId);
    Optional<MenuItemEntity> findByStoreIdAndName(UUID storeId, String name);
    List<MenuItemEntity> findByStoreIdAndAvailable(UUID storeId, boolean available);
}
