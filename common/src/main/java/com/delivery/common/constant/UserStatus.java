package com.delivery.common.constant;

public enum UserStatus {

    ACTIVE,
    INACTIVE,//활성화비활성화 라는게 유저아이디가 db에 있는가를 기준으로 보는건가? 아니면 현재 로그인 중인거를 하는건가?
    DELETED
}
