package com.delivery.common.exception;


public class PaymentFailedException extends BusinessException {
    public PaymentFailedException() {
        super(ErrorCode.PAYMENT_FAILED);
    }
}

