package com.delivery.domain.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * 주문 아이템 라인 도메인 엔티티
 * 
 * 리팩토링 사항:
 * 1. 불필요한 빈 줄 제거
 * 2. 주석 추가: 클래스와 메서드의 목적 설명
 */
@Getter
@AllArgsConstructor
public class ItemLine {
    private UUID menuItemId;
    private String menuItemName;
    private int unitPrice;
    private int quantity;

    /**
     * 이 주문 라인의 총 금액을 계산합니다.
     * 
     * 계산: 단가 × 수량
     * 
     * @return 주문 라인 총 금액
     */
    public int getLineAmount() {
        return unitPrice * quantity;
    }
}
