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

    /**
     * 주문을 결제 완료 상태로 변경합니다.
     * 
     * 리팩토링: 메서드 가독성 개선 (한 줄 -> 여러 줄)
     */
    public void markPaid() {
        this.status = OrderStatus.PAID;
        this.paidAt = OffsetDateTime.now();
    }

    /**
     * 주문을 준비 중 상태로 변경합니다.
     */
    public void startPreparing() {
        this.status = OrderStatus.PREPARING;
    }

    /**
     * 주문을 배송 중 상태로 변경합니다.
     */
    public void startDelivery() {
        this.status = OrderStatus.DELIVERING;
    }

    /**
     * 주문을 완료 상태로 변경합니다.
     * 
     * 리팩토링: 메서드 가독성 개선
     */
    public void complete() {
        this.status = OrderStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
    }

    /**
     * 주문을 취소 상태로 변경합니다.
     */
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * 주문 상세를 추가합니다.
     * 
     * @param detail 추가할 주문 상세
     * 
     * 리팩토링: null 검증 추가
     */
    public void addDetail(OrderDetailEntity detail) {
        if (detail == null) {
            throw new IllegalArgumentException("주문 상세는 null일 수 없습니다");
        }
        detail.attachTo(this);
        this.details.add(detail);
        recalcTotal();
    }

    /**
     * 주문 상세를 제거합니다.
     * 
     * @param detailId 제거할 주문 상세 ID
     */
    public void removeDetail(UUID detailId) {
        if (detailId == null) {
            return;
        }
        this.details.removeIf(d -> d.getId().equals(detailId));
        recalcTotal();
    }

    /**
     * 주문 총액을 재계산합니다.
     * 
     * 리팩토링: private 메서드로 캡슐화
     */
    private void recalcTotal() {
        this.totalAmount = details.stream()
                .mapToInt(OrderDetailEntity::getLineAmount)
                .sum();
    }
}
