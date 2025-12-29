package com.delivery.db.entity.payment;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;



@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payments_order", columnList = "orderId"),
                @Index(name = "idx_payments_status", columnList = "status")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {//결제는 일단 대강

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status;

    @Column(nullable = false)
    private OffsetDateTime requestedAt;

    @Column
    private OffsetDateTime paidAt;

    @Column(length = 64)
    private String provider;

    @Column(length = 128)
    private String transactionId;

    public void markSuccess(String transactionId) {
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = OffsetDateTime.now();
        this.transactionId = transactionId;
    }

    public void markFailed() { this.status = PaymentStatus.FAILED; }
    public void cancel() { this.status = PaymentStatus.CANCELLED; }
}
