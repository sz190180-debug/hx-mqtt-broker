package com.hx.mqtt.domain.req.mqtt;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

/**
 * MQTT 账户请求数据模型
 * <p>
 * 用于接收客户端发送的账户操作请求，
 * 对应JSON格式：{"action":"account","identifier":"","password":""}
 */
@Data
public class UserLoginReq {

    /**
     * 操作类型
     */
    private String action;

    /**
     * 用户标识
     * 可能是 clientId或手机号
     */
    private String identifier;

    /**
     * 密码
     */
    private String password;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的MqttAccountRequest对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static UserLoginReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, UserLoginReq.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的JSON格式", e);
        }
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @return JSON格式的字符串
     */
    public String toJson() {
        return JSONObject.toJSONString(this);
    }
}