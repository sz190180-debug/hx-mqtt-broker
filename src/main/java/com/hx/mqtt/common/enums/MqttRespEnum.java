package com.hx.mqtt.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MqttRespEnum {
    SUCCESS(10000, "成功"),
    NONE_EXIST_USER(10001, "用户校验失败"),
    NONE_EXIST_TOPIC(10002, "不存在的主题"),
    NONE_EXIST_CLIENT(10003, "不存在的clientId"),
    NONE_IMPL_TOPIC(10004, "未实现的主题功能"),
    NONE_EXIST_REQ_ID(10005, "不存在的请求id"),
    NONE_VERTEXES_CODE(10006, "地图点位code部分不存在或完全不存在,请核对点位"),
    NONE_AUTHORIZATION(10007, "未鉴权"),
    NONE_EXIST_STATUS(10008, "不存在状态"),
    TASK_TEMPLATE_FAILED(10009, ",发送失败请检查车辆是否在线"),

    EXCEPTION(11000, "系统异常");

    private final Integer code;
    private final String msg;
}
