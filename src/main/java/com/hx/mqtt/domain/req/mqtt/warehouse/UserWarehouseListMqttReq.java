package com.hx.mqtt.domain.req.mqtt.warehouse;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取用户仓库权限列表MQTT请求类
 */
@Data
public class UserWarehouseListMqttReq {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的UserWarehouseListMqttReq对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static UserWarehouseListMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, UserWarehouseListMqttReq.class);
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
