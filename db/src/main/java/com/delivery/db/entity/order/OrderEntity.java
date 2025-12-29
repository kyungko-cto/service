package com.delivery.db.entity.order;


import com.delivery.db.entity.store.StoreEntity;
import com.delivery.db.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_store", columnList = "store_id"),
                @Index(name = "idx_orders_status", columnList = "status")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private int totalAmount;

    private OffsetDateTime createdAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime completedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetailEntity> details = new ArrayList<>();

    public void markPaid() { this.status = OrderStatus.PAID; this.paidAt = OffsetDateTime.now(); }
    public void startPreparing() { this.status = OrderStatus.PREPARING; }
    public void startDelivery() { this.status = OrderStatus.DELIVERING; }
    public void complete() { this.status = OrderStatus.COMPLETED; this.completedAt = OffsetDateTime.now(); }
    public void cancel() { this.status = OrderStatus.CANCELLED; }

    public void addDetail(OrderDetailEntity detail) {
        detail.attachTo(this);
        this.details.add(detail);
        recalcTotal();
    }
    public void removeDetail(UUID detailId) {
        this.details.removeIf(d -> d.getId().equals(detailId));
        recalcTotal();
    }
    public void recalcTotal() {
        this.totalAmount = details.stream().mapToInt(OrderDetailEntity::getLineAmount).sum();
    }
}
