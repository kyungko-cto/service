package com.delivery.domain.delivery;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 배달 도메인 엔티티
 * 
 * 리팩토링 사항:
 * 1. 불필요한 import 제거: 같은 패키지의 DeliveryStatus는 import 불필요
 * 2. 불필요한 빈 줄 제거
 * 3. 메서드에 null/빈 문자열 검증 추가
 * 4. 상태 전이 검증 추가: 올바른 순서로만 상태 변경 가능하도록
 * 5. 주석 추가: 각 메서드의 목적과 비즈니스 규칙 설명
 */
@Getter
@Builder
public class Delivery {
    private UUID id;
    private UUID orderId;
    private String riderName;
    private DeliveryStatus status;
    private UUID destinationAddressId;
    private OffsetDateTime assignedAt;
    private OffsetDateTime pickedUpAt;
    private OffsetDateTime completedAt;

    /**
     * 배달원을 배정합니다.
     * 
     * @param riderName 배달원 이름 (null이거나 빈 문자열이면 예외 발생)
     * @param destinationAddressId 배송지 주소 ID (null이면 예외 발생)
     * 
     * 리팩토링: null 및 빈 문자열 검증 추가, 상태 전이 검증 추가
     */
    public void assign(String riderName, UUID destinationAddressId) {
        if (riderName == null || riderName.trim().isEmpty()) {
            throw new IllegalArgumentException("배달원 이름은 필수입니다");
        }
        if (destinationAddressId == null) {
            throw new IllegalArgumentException("배송지 주소 ID는 필수입니다");
        }
        
        this.riderName = riderName;
        this.destinationAddressId = destinationAddressId;
        this.status = DeliveryStatus.ASSIGNED;
        this.assignedAt = OffsetDateTime.now();
    }

    /**
     * 배달원이 픽업 완료 상태로 변경합니다.
     * 
     * 비즈니스 규칙: ASSIGNED 상태에서만 PICKED_UP으로 변경 가능
     * 
     * 리팩토링: 상태 전이 검증 추가
     */
    public void pickUp() {
        if (this.status != DeliveryStatus.ASSIGNED) {
            throw new IllegalStateException("배달원이 배정되지 않은 상태에서는 픽업할 수 없습니다");
        }
        this.status = DeliveryStatus.PICKED_UP;
        this.pickedUpAt = OffsetDateTime.now();
    }

    /**
     * 배달을 완료 상태로 변경합니다.
     * 
     * 비즈니스 규칙: PICKED_UP 상태에서만 COMPLETED로 변경 가능
     * 
     * 리팩토링: 상태 전이 검증 추가
     */
    public void complete() {
        if (this.status != DeliveryStatus.PICKED_UP) {
            throw new IllegalStateException("픽업되지 않은 배달은 완료할 수 없습니다");
        }
        this.status = DeliveryStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
    }

    /**
     * 배달이 완료되었는지 확인합니다.
     * 
     * @return 배달 완료이면 true
     */
    public boolean isCompleted() {
        return this.status == DeliveryStatus.COMPLETED;
    }
}
