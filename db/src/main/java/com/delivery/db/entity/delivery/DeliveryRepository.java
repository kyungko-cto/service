package com.delivery.db.entity.delivery;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, UUID> {
    List<DeliveryEntity> findByOrderIdOrderByAssignedAtDesc(UUID orderId);
    List<DeliveryEntity> findByStatusOrderByAssignedAtDesc(DeliveryStatus status);
}
