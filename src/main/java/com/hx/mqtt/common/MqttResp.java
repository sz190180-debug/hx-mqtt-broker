package com.hx.mqtt.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.enums.MqttRespEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Getter
@Setter
@RequiredArgsConstructor
public class MqttResp<T> {

    private static final DateTimeFormatter TS_FORMATTER =
            new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T" + "'HH:mm:ss").appendFraction(ChronoField.MICRO_OF_SECOND, 6, 6, true).toFormatter();

    private final Long reqId;
    private final Integer code;
    private final String msg;
    private final T data;

    public MqttResp(Long reqId, MqttRespEnum respEnum) {
        this(reqId, respEnum.getCode(), respEnum.name(), null);
    }

    public MqttResp(Long reqId, MqttRespEnum respEnum, T data) {
        this(reqId, respEnum.getCode(), respEnum.name(), data);
    }


    public static <T> MqttResp<T> success(Long reqId) {
        return new MqttResp<>(reqId, MqttRespEnum.SUCCESS, null);
    }

    public static <T> MqttResp<T> success(T data) {
        return new MqttResp<>(null, MqttRespEnum.SUCCESS, data);
    }

    public static <T> MqttResp<T> success(Long reqId, T data) {
        return new MqttResp<>(reqId, MqttRespEnum.SUCCESS, data);
    }

    public static MqttResp<String> fail(Long reqId, String msg) {
        return new MqttResp<>(reqId, MqttRespEnum.EXCEPTION.getCode(), msg, null);
    }

    public static MqttResp<String> fail(Long reqId, MqttRespEnum respEnum) {
        return new MqttResp<>(reqId, respEnum);
    }


    @Override
    public String toString() {
        JSONObject result = new JSONObject();
        JSONObject d = new JSONObject();

        // 将 reqId 放入 d 下（非空时展示）
        if (this.reqId != null) {
            d.put("reqId", this.reqId);
        }
        if (this.code != null) {
            d.put("code", this.code);
        }
        if (this.msg != null) {
            d.put("msg", this.msg);
        }

        // 平铺 data 到 d 下
        if (this.data != null) {
            Object dataJson = JSON.toJSON(this.data);
            if (dataJson instanceof JSONObject) {
                d.putAll((JSONObject) dataJson);
            } else {
                // 处理 data 是基本类型的情况（如 String/Number）
                d.put("value", dataJson);
            }
        }

        result.put("d", d);
        result.put("ts", LocalDateTime.now().format(TS_FORMATTER));

        return result.toJSONString();
    }
}
