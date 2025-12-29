package com.delivery.db.entity.order;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;




@Entity
@Table(name = "order_detail",
        indexes = {
                @Index(name = "idx_order_detail_order", columnList = "order_id")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID menuItemId;

    @Column(nullable = false)
    private String menuItemName;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private OrderEntity order;


    public void changeQuantity(int newQty) { this.quantity = newQty; }
    public int getLineAmount() { return unitPrice * quantity; }


    void attachTo(OrderEntity order) { this.order = order; }
}
