package com.delivery.domain.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * 장바구니 아이템 (상품) 도메인 엔티티
 *
 * Serializable 구현 이유:
 * - Redis에 직렬화하여 저장하기 위함
 * - Cart와 함께 저장되므로 반드시 Serializable 구현 필요
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID menuItemId;      // 메뉴 상품 ID
    private UUID storeId;         // 가게 ID (어느 가게 상품인지)
    private String menuItemName;  // 상품명 (장바구니 표시용)
    private int unitPrice;        // 단가
    private int quantity;         // 수량

    /**
     * 이 아이템의 소계 금액 계산
     *
     * 계산: unitPrice * quantity
     *
     * 이유:
     * - 클라이언트 계산 방지 (가격 조작 방지)
     * - 할인/세금 로직이 추가되어도 서버에서 중앙 관리
     */
    public int getLineAmount() {
        return unitPrice * quantity;
    }

    /**
     * 이 아이템의 수량 증가
     *
     * @param additionalQuantity 추가할 수량
     *
     * 이유:
     * - 같은 상품을 여러 번 담을 때 수량만 증가
     * - 비즈니스 로직을 도메인에 캡슐화
     */
    public void addQuantity(int additionalQuantity) {
        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException("추가 수량은 0보다 커야 합니다");
        }

        this.quantity += additionalQuantity;
    }

    /**
     * 수량 수정 (수정 API에서 사용)
     *
     * @param newQuantity 새로운 수량
     *
     * 주의:
     * - 0 이상만 허용 (0이면 removeItem으로 제거해야 함)
     */
    public void setQuantity(int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다");
        }

        this.quantity = newQuantity;
    }

    /**
     * 이 아이템이 유효한지 검증
     *
     * 이유: 장바구니에 추가하기 전 기본 검증
     */
    public void validate() {
        if (menuItemId == null) {
            throw new IllegalArgumentException("메뉴 아이템 ID는 필수입니다");
        }

        if (storeId == null) {
            throw new IllegalArgumentException("가게 ID는 필수입니다");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
        }

        if (unitPrice < 0) {
            throw new IllegalArgumentException("단가는 0 이상이어야 합니다");
        }
    }
}