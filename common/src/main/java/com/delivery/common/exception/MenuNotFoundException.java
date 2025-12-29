package com.delivery.common.exception;

public class MenuNotFoundException extends BusinessException {
    public MenuNotFoundException() {
        super(ErrorCode.MENU_NOT_FOUND);
    }
}
