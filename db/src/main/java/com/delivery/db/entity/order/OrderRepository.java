package com.delivery.db.entity.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<OrderEntity> findByStoreIdAndStatusOrderByCreatedAtDesc(UUID storeId, OrderStatus status);
    List<OrderEntity> findByStoreIdOrderByCreatedAtDesc(UUID storeId);
}
