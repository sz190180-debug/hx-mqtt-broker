package com.hx.mqtt.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserTypeEnum {

    CLIENT(1, "显示屏"),
    APP(2, "app");

    private final Integer code;
    private final String msg;
}
