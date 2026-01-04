package com.delivery.domain.menu;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 메뉴 도메인 엔티티
 * 
 * 리팩토링 사항:
 * 1. 불필요한 빈 줄 제거
 * 2. 메서드에 null/빈 문자열 검증 추가
 * 3. 예외 메시지를 한국어로 통일
 * 4. 주석 추가: 각 메서드의 목적과 비즈니스 규칙 설명
 */
@Getter
@Builder
public class Menu {
    private UUID id;
    private UUID storeId;
    private String name;
    private String description;
    private int price;
    private MenuCategory category;
    private MenuStatus status;

    /**
     * 메뉴 이름을 변경합니다.
     * 
     * @param name 새로운 메뉴 이름 (null이거나 빈 문자열이면 예외 발생)
     * 
     * 리팩토링: null 및 빈 문자열 검증 추가
     */
    public void updateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("메뉴 이름은 비워둘 수 없습니다");
        }
        this.name = name;
    }

    /**
     * 메뉴 설명을 변경합니다.
     * 
     * @param description 새로운 메뉴 설명 (null 허용, 빈 문자열 허용)
     */
    public void updateDescription(String description) {
        this.description = description;
    }

    /**
     * 메뉴 가격을 변경합니다.
     * 
     * @param price 새로운 가격 (0 이상이어야 함)
     * 
     * 리팩토링: 예외 메시지를 한국어로 통일
     */
    public void updatePrice(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
        }
        this.price = price;
    }

    /**
     * 메뉴 카테고리를 변경합니다.
     * 
     * @param category 새로운 카테고리 (null이면 예외 발생)
     * 
     * 리팩토링: null 검증 추가
     */
    public void changeCategory(MenuCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 null일 수 없습니다");
        }
        this.category = category;
    }

    /**
     * 메뉴를 주문 가능 상태로 변경합니다.
     */
    public void markAvailable() {
        this.status = MenuStatus.AVAILABLE;
    }

    /**
     * 메뉴를 주문 불가능 상태로 변경합니다.
     * 
     * 비즈니스 규칙: 재고 부족, 품절 등의 경우 사용
     */
    public void markUnavailable() {
        this.status = MenuStatus.UNAVAILABLE;
    }

    /**
     * 메뉴가 현재 주문 가능한지 확인합니다.
     * 
     * @return 주문 가능하면 true
     */
    public boolean isAvailable() {
        return this.status == MenuStatus.AVAILABLE;
    }
}
