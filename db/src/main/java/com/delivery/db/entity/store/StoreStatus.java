package com.delivery.db.entity.store;

/**
 * 가게 상태 열거형
 * 
 * 리팩토링: SUSPENDED 상태 추가 (관리자에 의한 정지)
 */
public enum StoreStatus {
    OPEN,      // 영업 중
    CLOSED,    // 영업 종료
    PAUSED,    // 일시 휴업
    SUSPENDED  // 정지 (관리자에 의한 강제 정지)
}
