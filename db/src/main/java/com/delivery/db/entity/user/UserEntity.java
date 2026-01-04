package com.delivery.db.entity.user;

import com.delivery.db.entity.common.AddressEntity;
import com.delivery.db.entity.store.StoreEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 사용자 엔티티
 *
 * 테이블: users
 * 역할: 사용자 정보 저장 (회원가입, 프로필 관리)
 *
 * 특징:
 * - UUID로 PK 관리 (분산 환경 대비)
 * - 이메일, 사용자명 unique 제약
 * - 주소, 가게 등 관계 관리
 * - 감시 필드 (생성일, 수정일)
 */
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true),
                @Index(name = "idx_users_username", columnList = "username", unique = true),
                @Index(name = "idx_users_role", columnList = "role"),
                @Index(name = "idx_users_status", columnList = "status")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    /**
     * 비밀번호 (암호화됨)
     *
     * 이름: passwordHash (password 대신)
     * 이유: 암호화된 상태임을 명시적으로 표현
     * 길이: 255 (BCrypt 해시 길이)
     */
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /**
     * 사용자 권한
     *
     * 값: ROLE_USER, ROLE_OWNER, ROLE_ADMIN
     * Enum 사용: 타입 안전성, 유효한 값만 저장
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * 사용자 등급 (멤버십)
     *
     * 값: BRONZE(기본), SILVER, GOLD 등
     * 사용 예: 할인율, 배송비 감면 등
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Grade grade = Grade.BRONZE;

    /**
     * 계정 상태
     *
     * 값:
     * - ACTIVE: 정상 사용 가능
     * - SUSPENDED: 일시 중지 (관리자가 잠금)
     * - DELETED: 삭제됨 (탈퇴)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * 사용자의 배송 주소 목록
     *
     * 관계: 1:N (사용자 1명당 여러 주소)
     * 로딩: LAZY (필요할 때만 조회)
     * 삭제: 사용자 삭제 시 주소도 함께 삭제 (CascadeType.ALL)
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AddressEntity> addresses = new ArrayList<>();

    /**
     * 사용자가 소유한 가게 목록
     *
     * 관계: 1:N (사용자/점주 1명당 여러 가게)
     * 로딩: LAZY (필요할 때만 조회)
     * 삭제: 명시적으로만 삭제 (가게 보존)
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StoreEntity> stores = new ArrayList<>();

    /**
     * 레코드 생성 시간 (자동)
     *
     * @CreatedDate: JPA Auditing으로 자동 설정
     * 설정 필요: @EnableJpaAuditing
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 레코드 수정 시간 (자동)
     *
     * @LastModifiedDate: JPA Auditing으로 자동 업데이트
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ===== 도메인 메서드 =====

    /**
     * 이메일 변경
     *
     * 이유: 엔티티에 비즈니스 로직 캡슐화
     */
    public void changeEmail(String newEmail) {
        if (newEmail == null || newEmail.isEmpty()) {
            throw new IllegalArgumentException("이메일은 비워둘 수 없습니다");
        }
        this.email = newEmail;
    }

    /**
     * 전화번호 변경
     */
    public void changePhoneNumber(String newPhoneNumber) {
        if (newPhoneNumber == null || newPhoneNumber.isEmpty()) {
            throw new IllegalArgumentException("전화번호는 비워둘 수 없습니다");
        }
        this.phoneNumber = newPhoneNumber;
    }

    /**
     * 비밀번호 변경 (암호화된 상태)
     *
     * 주의: 이 메서드는 이미 암호화된 해시를 받음
     * 서비스에서 PasswordEncoder.encode()를 먼저 호출할 것
     */
    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 비워둘 수 없습니다");
        }
        this.passwordHash = newPasswordHash;
    }

    /**
     * 주소 추가
     *
     * 양방향 관계 설정:
     * 1. 사용자 → 주소 추가
     * 2. 주소 → 사용자 설정 (양쪽 동기화)
     */
    public void addAddress(AddressEntity address) {
        if (address == null) {
            throw new IllegalArgumentException("주소는 null일 수 없습니다");
        }

        address.attachToUser(this);  // 양쪽 관계 설정
        this.addresses.add(address);
    }

    /**
     * 주소 제거
     */
    public void removeAddress(UUID addressId) {
        this.addresses.removeIf(a -> a.getId().equals(addressId));
    }

    /**
     * 사용자 등급 업그레이드 (멤버십)
     *
     * 사용 예: 주문 누적액 도달 시 등급 상향
     */
    public void upgradeGrade(Grade newGrade) {
        if (newGrade == null) {
            throw new IllegalArgumentException("등급은 null일 수 없습니다");
        }
        this.grade = newGrade;
    }

    /**
     * 계정 일시 중지 (관리자 기능)
     *
     * 상태: ACTIVE → SUSPENDED
     */
    public void suspend() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("활성 계정만 중지할 수 있습니다");
        }
        this.status = AccountStatus.SUSPENDED;
    }

    /**
     * 계정 활성화 (관리자 기능)
     *
     * 상태: SUSPENDED → ACTIVE
     */
    public void activate() {
        if (this.status != AccountStatus.SUSPENDED) {
            throw new IllegalStateException("중지된 계정만 활성화할 수 있습니다");
        }
        this.status = AccountStatus.ACTIVE;
    }

    /**
     * 계정 삭제 (탈퇴)
     *
     * 상태: ACTIVE/SUSPENDED → DELETED
     * 이유: 실제 삭제가 아닌 상태 변경 (감시 목적, 복구 대비)
     */
    public void delete() {
        this.status = AccountStatus.DELETED;
    }

    /**
     * 계정이 활성 상태인지 확인
     */
    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    /**
     * 계정이 점주인지 확인
     */
    public boolean isOwner() {
        return this.role == Role.OWNER || this.role == Role.ADMIN;
    }
}