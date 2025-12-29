package com.delivery.db.entity.payment;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;



public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    List<PaymentEntity> findByOrderIdOrderByRequestedAtDesc(UUID orderId);
    Optional<PaymentEntity> findTopByOrderIdOrderByRequestedAtDesc(UUID orderId);
    Optional<PaymentEntity> findByTransactionId(String transactionId);
}
