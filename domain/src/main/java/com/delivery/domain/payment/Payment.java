package com.delivery.domain.payment;


import com.delivery.domain.payment.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Payment {
    private UUID id;
    private UUID orderId;
    private int amount;
    private PaymentStatus status;
    private String provider;
    private String transactionId;

    public void markSuccess(String transactionId) {
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }
}
