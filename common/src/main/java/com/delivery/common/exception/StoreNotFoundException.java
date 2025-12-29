package com.delivery.common.exception;

public class StoreNotFoundException extends BusinessException {
    public StoreNotFoundException() {
        super(ErrorCode.STORE_NOT_FOUND);
    }
}
