package com.hx.mqtt.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserLoginActionEnum {
    CLIENT_ID("client_id"),
    ACCOUNT("account");

    private final String code;

    public static UserLoginActionEnum getByCode(String code) {
        for (UserLoginActionEnum userLoginActionEnum : UserLoginActionEnum.values()) {
            if (userLoginActionEnum.getCode().equals(code)) {
                return userLoginActionEnum;
            }
        }
        return null;
    }
}
