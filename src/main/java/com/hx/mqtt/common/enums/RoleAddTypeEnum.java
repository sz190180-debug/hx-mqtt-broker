package com.hx.mqtt.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleAddTypeEnum {

    ADD(1, "追加"),
    REPLACE(2, "覆盖");

    private final Integer code;
    private final String msg;
}
