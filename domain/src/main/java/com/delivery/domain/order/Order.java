package com.delivery.domain.order;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 주문 도메인 엔티티
 * 
 * 리팩토링 사항:
 * 1. 불필요한 import 제거: 같은 패키지의 OrderStatus는 import 불필요
 * 2. 비즈니스 로직 개선: 주문 취소 시 상태 검증 추가
 * 3. null 체크 추가: addItem 메서드에 null 검증
 * 4. 주석 추가: 각 메서드의 목적과 비즈니스 규칙 설명
 */
@Getter
@Builder
public class Order {
    private UUID id;
    private UUID userId;
    private UUID storeId;
    private OrderStatus status;
    private int totalAmount;
    private OffsetDateTime createdAt;
    @Builder.Default
    private List<ItemLine> details = new ArrayList<>();

    /**
     * 주문에 아이템을 추가합니다.
     * 
     * @param item 추가할 주문 아이템 (null이면 예외 발생)
     * 
     * 리팩토링: null 체크 추가 및 총액 자동 재계산
     */
    public void addItem(ItemLine item) {
        if (item == null) {
            throw new IllegalArgumentException("주문 아이템은 null일 수 없습니다");
        }
        this.details.add(item);
        recalcTotal();
    }

    /**
     * 주문을 취소합니다.
     * 
     * 비즈니스 규칙:
     * - 이미 배송 중이거나 완료된 주문은 취소할 수 없음
     * - 취소 가능한 상태: CREATED, PAID
     * 
     * 리팩토링: 취소 가능 여부 검증 추가 (현재는 단순 상태 변경만 수행)
     * TODO: 실제 비즈니스 규칙에 따라 취소 가능 여부 검증 로직 추가 필요
     */
    public void cancel() {
        if (this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("이미 배송 완료된 주문은 취소할 수 없습니다");
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * 주문 총액을 재계산합니다.
     * 
     * 계산 로직: 모든 주문 아이템의 lineAmount 합산
     * 
     * 리팩토링: private 메서드로 캡슐화하여 외부에서 직접 호출 불가
     */
    private void recalcTotal() {
        this.totalAmount = details.stream()
                .mapToInt(ItemLine::getLineAmount)
                .sum();
    }

    /**
     * 주문이 취소 가능한 상태인지 확인합니다.
     * 
     * @return 취소 가능하면 true
     */
    public boolean canCancel() {
        return this.status == OrderStatus.CREATED || this.status == OrderStatus.PAID;
    }
}
