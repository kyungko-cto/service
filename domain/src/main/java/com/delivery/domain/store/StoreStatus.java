package com.delivery.domain.store;

/**
 * 가게 상태 열거형
 * 
 * 리팩토링: 주석 추가
 */
public enum StoreStatus {
    OPEN,      // 영업 중
    CLOSED,    // 영업 종료
    SUSPENDED  // 정지 (관리자에 의한 강제 정지)
}
