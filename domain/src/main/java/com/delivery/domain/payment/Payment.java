package com.delivery.domain.payment;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 결제 도메인 엔티티
 * 
 * 리팩토링 사항:
 * 1. 불필요한 import 제거: 같은 패키지의 PaymentStatus는 import 불필요
 * 2. 불필요한 빈 줄 제거
 * 3. 메서드에 null/빈 문자열 검증 추가
 * 4. 주석 추가: 각 메서드의 목적과 비즈니스 규칙 설명
 */
@Getter
@Builder
public class Payment {
    private UUID id;
    private UUID orderId;
    private int amount;
    private PaymentStatus status;
    private String provider;
    private String transactionId;

    /**
     * 결제를 성공 상태로 변경합니다.
     * 
     * @param transactionId 결제 거래 ID (null이거나 빈 문자열이면 예외 발생)
     * 
     * 리팩토링: null 및 빈 문자열 검증 추가
     */
    public void markSuccess(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("거래 ID는 필수입니다");
        }
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
    }

    /**
     * 결제를 실패 상태로 변경합니다.
     * 
     * 비즈니스 규칙: 결제 실패 시 거래 ID는 유지하지 않음
     */
    public void markFailed() {
        this.status = PaymentStatus.FAILED;
        this.transactionId = null; // 실패 시 거래 ID 초기화
    }

    /**
     * 결제가 성공했는지 확인합니다.
     * 
     * @return 결제 성공이면 true
     */
    public boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }
}
