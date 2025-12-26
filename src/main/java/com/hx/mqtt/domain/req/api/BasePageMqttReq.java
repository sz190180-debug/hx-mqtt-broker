package com.hx.mqtt.domain.req.api;

import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BasePageMqttReq extends BasePageReq {

    public static BasePageMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, BasePageMqttReq.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的JSON格式", e);
        }
    }
}
