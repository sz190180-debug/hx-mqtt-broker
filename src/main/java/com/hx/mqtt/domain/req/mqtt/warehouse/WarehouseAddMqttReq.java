package com.hx.mqtt.domain.req.mqtt.warehouse;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 添加仓库MQTT请求类
 */
@Data
public class WarehouseAddMqttReq {

    /**
     * 仓库名称
     */
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;

    /**
     * 仓库描述
     */
    private String description;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的WarehouseAddMqttReq对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static WarehouseAddMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, WarehouseAddMqttReq.class);
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
