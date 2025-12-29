package com.delivery.db.entity.menu;


import com.delivery.db.entity.store.StoreEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Entity
@Table(name = "menu_items",
        indexes = {
                @Index(name = "idx_menu_store", columnList = "store_id"),
                @Index(name = "idx_menu_name_store", columnList = "name,store_id", unique = true)
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean available;

    // ===== 도메인 메서드 =====
    public void rename(String newName) { this.name = newName; }
    public void changePrice(int newPrice) { this.price = newPrice; }
    public void changeDescription(String desc) { this.description = desc; }
    public void setAvailable(boolean available) { this.available = available; }

    public void attachTo(StoreEntity store) { this.store = store; }
}
