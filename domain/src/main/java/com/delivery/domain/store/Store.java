package com.delivery.domain.store;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 가게 도메인 엔티티
 * 
 * 리팩토링 사항:
 * 1. 불필요한 빈 줄 제거
 * 2. 메서드에 null/빈 문자열 검증 추가
 * 3. 주석 추가: 각 메서드의 목적과 비즈니스 규칙 설명
 */
@Getter
@Builder
public class Store {
    private UUID id;
    private String name;
    private String address;
    private StoreStatus status;
    private String phone;

    /**
     * 가게 이름을 변경합니다.
     * 
     * @param name 새로운 가게 이름 (null이거나 빈 문자열이면 예외 발생)
     * 
     * 리팩토링: null 및 빈 문자열 검증 추가
     */
    public void rename(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("가게 이름은 비워둘 수 없습니다");
        }
        this.name = name;
    }

    /**
     * 가게 주소를 변경합니다.
     * 
     * @param address 새로운 주소 (null이거나 빈 문자열이면 예외 발생)
     * 
     * 리팩토링: null 및 빈 문자열 검증 추가
     */
    public void relocate(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("주소는 비워둘 수 없습니다");
        }
        this.address = address;
    }

    /**
     * 가게 전화번호를 변경합니다.
     * 
     * @param phone 새로운 전화번호 (null이거나 빈 문자열이면 예외 발생)
     * 
     * 리팩토링: null 및 빈 문자열 검증 추가
     */
    public void changePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 비워둘 수 없습니다");
        }
        this.phone = phone;
    }

    /**
     * 가게를 영업 중 상태로 변경합니다.
     */
    public void open() {
        this.status = StoreStatus.OPEN;
    }

    /**
     * 가게를 영업 종료 상태로 변경합니다.
     */
    public void close() {
        this.status = StoreStatus.CLOSED;
    }

    /**
     * 가게를 일시 중지 상태로 변경합니다.
     * 
     * 비즈니스 규칙: 관리자에 의한 강제 중지
     */
    public void suspend() {
        this.status = StoreStatus.SUSPENDED;
    }

    /**
     * 가게가 현재 영업 중인지 확인합니다.
     * 
     * @return 영업 중이면 true
     */
    public boolean isOpen() {
        return this.status == StoreStatus.OPEN;
    }
}

