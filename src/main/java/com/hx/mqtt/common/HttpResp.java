package com.hx.mqtt.common;

import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.enums.MqttRespEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class HttpResp<T> {

    private final Integer code;
    private final String msg;
    private final T data;

    public HttpResp(MqttRespEnum respEnum) {
        this(respEnum.getCode(), respEnum.getMsg(), null);
    }

    public HttpResp(MqttRespEnum respEnum, T data) {
        this(respEnum.getCode(), respEnum.getMsg(), data);
    }

    public static <T> HttpResp<T> success() {
        return new HttpResp<>(MqttRespEnum.SUCCESS, null);
    }

    public static <T> HttpResp<T> success(T data) {
        return new HttpResp<>(MqttRespEnum.SUCCESS, data);
    }

    public static HttpResp<String> fail(String msg) {
        return new HttpResp<>(MqttRespEnum.EXCEPTION.getCode(), msg, null);
    }

    public static HttpResp<String> error(String msg) {
        return new HttpResp<>(MqttRespEnum.EXCEPTION.getCode(), msg, null);
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
