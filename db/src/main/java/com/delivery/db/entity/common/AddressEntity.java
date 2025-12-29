package com.delivery.db.entity.common;


import com.delivery.db.entity.user.UserEntity;
import com.delivery.db.entity.store.StoreEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "addresses",
        indexes = {
                @Index(name = "idx_addresses_city", columnList = "city"),
                @Index(name = "idx_addresses_district", columnList = "district"),
                @Index(name = "idx_addresses_postal", columnList = "postalCode"),
                @Index(name = "idx_addresses_user", columnList = "user_id"),
                @Index(name = "idx_addresses_store", columnList = "store_id")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 64)
    private String city; // 시/도

    @Column(nullable = false, length = 64)
    private String district; // 구/군

    @Column(nullable = false, length = 128)
    private String street; // 도로명/지번

    @Column(length = 128)
    private String detail; // 상세주소

    @Column(length = 16)
    private String postalCode; // 우편번호

    // 소유 유저(선택) - 유저가 가진 주소(집/회사 등)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // 소속 매장(선택) - 매장 지점 주소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private StoreEntity store;

    // ===== 도메인 메서드 =====
    public void changeStreet(String newStreet) { this.street = newStreet; }
    public void changeDetail(String newDetail) { this.detail = newDetail; }
    public void changePostalCode(String newPostalCode) { this.postalCode = newPostalCode; }

    public void attachToUser(UserEntity user) { this.user = user; }
    public void attachToStore(StoreEntity store) { this.store = store; }
}
