package com.hx.mqtt.common.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum StateEnum {
    IDLE(1, "空闲"),
    WORKING(2, "工作中"),
    CHARGING(3, "充电中"),
    TRAFFIC_WAITING(4, "交管等待"),
    ESCROW(5, "托管中"),
    EXTERNAL_INTERRUPTION(6, "外部中断"),
    UNKNOWN(7, "未知"),
    ABNORMAL_PAUSE(8, "异常暂停"),
    WAITING_SCHEDULE_CONFIRMATION(1000, "等待调度确认");

    private static final Map<String, StateEnum> MSG_TO_ENUM = Arrays.stream(values())
            .collect(Collectors.toMap(StateEnum::getMsg, Function.identity()));
    private final int code;
    private final String msg;

    public static StateEnum fromMsg(String msg) {
        if (StrUtil.isEmpty(msg)) {
            return null;
        }
        return MSG_TO_ENUM.get(msg);
    }
}
