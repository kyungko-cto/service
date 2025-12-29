package com.delivery.common.exception;


public class DeliveryNotFoundException extends BusinessException {
    public DeliveryNotFoundException() {
        super(ErrorCode.DELIVERY_NOT_FOUND);
    }
}
