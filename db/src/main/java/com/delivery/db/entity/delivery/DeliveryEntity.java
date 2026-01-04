package com.delivery.db.entity.delivery;



import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;



@Entity
@Table(name = "deliveries",
        indexes = {
                @Index(name = "idx_deliveries_order", columnList = "orderId"),
                @Index(name = "idx_deliveries_status", columnList = "status")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(length = 64)
    private String riderName;

    @Column
    private UUID destinationAddressId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeliveryStatus status;

    @Column
    private OffsetDateTime assignedAt;

    @Column
    private OffsetDateTime pickedUpAt;

    @Column
    private OffsetDateTime completedAt;

    // 도메인 메서드
    public void assign(String riderName) {
        this.riderName = riderName;
        this.status = DeliveryStatus.ASSIGNED;
        this.assignedAt = OffsetDateTime.now();
    }

    public void pickUp() {
        this.status = DeliveryStatus.PICKED_UP;
        this.pickedUpAt = OffsetDateTime.now();
    }

    public void startDelivering() { this.status = DeliveryStatus.DELIVERING; }

    public void complete() {
        this.status = DeliveryStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
    }

    public void cancel() { this.status = DeliveryStatus.CANCELLED; }
}
