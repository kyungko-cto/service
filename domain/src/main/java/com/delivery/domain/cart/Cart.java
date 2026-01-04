package com.delivery.domain.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 장바구니 도메인 엔티티
 *
 * Serializable 구현 이유:
 * - Redis에 객체를 직렬화하여 저장하기 위함
 * - serialVersionUID: 버전 관리로 역직렬화 호환성 보장
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID userId;           // 사용자 ID
    private UUID storeId;          // 가게 ID (처음 담은 상품의 가게)
    private List<CartItem> items;  // 장바구니 아이템 목록

    /**
     * 장바구니에 상품 추가
     *
     * 비즈니스 로직:
     * 1. 같은 상품이 이미 있으면 수량 증가
     * 2. 없으면 새로운 아이템 추가
     * 3. 처음 담는 상품이면 storeId 설정
     */
    public void addItem(CartItem newItem) {
        // null 체크
        if (newItem == null) {
            throw new IllegalArgumentException("상품 정보는 필수입니다");
        }

        // items 리스트 초기화 (null 방지)
        if (items == null) {
            items = new ArrayList<>();
        }

        // 같은 메뉴 아이템이 이미 있는지 확인
        CartItem existingItem = items.stream()
                .filter(item -> item.getMenuItemId().equals(newItem.getMenuItemId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // 이미 있으면 수량만 증가
            // 이유: 같은 상품을 여러 번 클릭해도 리스트에 중복 추가 안 함
            existingItem.addQuantity(newItem.getQuantity());
        } else {
            // 없으면 새 아이템 추가
            items.add(newItem);

            // 처음 담는 상품이면 storeId 설정
            // 이유: 배달의민족은 한 가게에서만 주문 가능
            if (storeId == null) {
                storeId = newItem.getStoreId();
            }
        }
    }

    /**
     * 장바구니에서 특정 상품 제거
     *
     * 비즈니스 로직:
     * 1. 해당 상품을 찾아서 제거
     * 2. 제거 후 장바구니가 비면 storeId 초기화
     * 3. 없는 상품이면 아무것도 안 함 (idempotent)
     */
    public void removeItem(Object menuItemId) {
        // null 체크
        if (menuItemId == null) {
            return;
        }

        // items가 null이면 아무것도 안 함
        if (items == null || items.isEmpty()) {
            return;
        }

        // 해당 메뉴 아이템을 찾아서 제거
        // removeIf: 조건을 만족하는 요소를 모두 제거
        items.removeIf(item -> item.getMenuItemId().equals(menuItemId));

        // 제거 후 장바구니가 비면 storeId 초기화
        // 이유: 다음에 다른 가게 상품을 담을 수 있도록
        if (items.isEmpty()) {
            storeId = null;
        }
    }

    /**
     * 장바구니의 총 금액 계산
     *
     * 계산 로직:
     * - 모든 아이템의 lineAmount(단가 * 수량)을 합산
     * - items가 null이거나 비어있으면 0 반환
     */
    public int getTotalAmount() {
        // null 체크 및 빈 리스트 처리
        if (items == null || items.isEmpty()) {
            return 0;
        }

        // 모든 아이템의 금액을 합산
        // 이유: 클라이언트가 임의로 계산하지 않도록 서버에서 정확히 계산
        return items.stream()
                .mapToInt(CartItem::getLineAmount)
                .sum();
    }

    /**
     * 장바구니가 비어있는지 확인
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * 같은 가게의 상품만 담을 수 있는지 검증
     *
     * 이유: 배달의민족은 한 번에 한 가게에서만 주문 가능
     * @param newStoreId 추가하려는 상품의 가게 ID
     * @return true면 같은 가게, false면 다른 가게
     */
    public boolean isSameStore(UUID newStoreId) {
        // 장바구니가 비어있으면 true (첫 상품이므로 추가 가능)
        if (isEmpty()) {
            return true;
        }

        // 같은 가게면 true, 다른 가게면 false
        return storeId.equals(newStoreId);
    }
}