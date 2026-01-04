package com.delivery.db.entity.store;


import com.delivery.db.entity.common.AddressEntity;
import com.delivery.db.entity.menu.MenuItemEntity;
import com.delivery.db.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "stores",
        indexes = {
                @Index(name = "idx_stores_owner", columnList = "owner_id"),
                @Index(name = "idx_stores_status", columnList = "status")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreEntity {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @Enumerated(EnumType.STRING)
    private StoreStatus status;

    @Column(length = 20)
    private String phone;

    private LocalTime openTime;
    private LocalTime closeTime;

    // 매장 주소 (1:1)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", nullable = false)
    private AddressEntity address;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuItemEntity> menuItems = new ArrayList<>();

    // ===== 도메인 메서드 =====
    public void rename(String newName) { this.name = newName; }
    public void changeHours(LocalTime newOpen, LocalTime newClose) { this.openTime = newOpen; this.closeTime = newClose; }
    public void open() { this.status = StoreStatus.OPEN; }
    public void close() { this.status = StoreStatus.CLOSED; }
    public void pause() { this.status = StoreStatus.PAUSED; }
    public void suspend() { this.status = StoreStatus.SUSPENDED; }

    public void changeAddress(AddressEntity newAddress) { this.address = newAddress; }

    public void addMenuItem(MenuItemEntity item) {
        item.attachTo(this);
        this.menuItems.add(item);
    }
}


