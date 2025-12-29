package com.delivery.db.entity.user;


import com.delivery.db.entity.common.AddressEntity;
import com.delivery.db.entity.store.StoreEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;


@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true),
                @Index(name = "idx_users_username", columnList = "username", unique = true),
                @Index(name = "idx_users_role", columnList = "role")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AddressEntity> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @Builder.Default
    private List<StoreEntity> stores = new ArrayList<>();

    // ===== 도메인 메서드 =====
    public void changeEmail(String newEmail) { this.email = newEmail; }
    public void changePhoneNumber(String newPhoneNumber) { this.phoneNumber = newPhoneNumber; }
    public void changePasswordHash(String newHash) { this.passwordHash = newHash; }

    public void addAddress(AddressEntity address) {
        address.attachToUser(this);
        this.addresses.add(address);
    }
    public void removeAddress(UUID addressId) {
        this.addresses.removeIf(a -> a.getId().equals(addressId));
    }

    public void upgradeGrade(Grade newGrade) { this.grade = newGrade; }
    public void suspend() { this.status = AccountStatus.SUSPENDED; }
    public void activate() { this.status = AccountStatus.ACTIVE; }
    public void delete() { this.status = AccountStatus.DELETED; }
}
