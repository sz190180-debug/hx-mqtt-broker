package com.hx.mqtt.domain.req.mqtt.warehouse;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新仓库MQTT请求类
 */
@Data
public class WarehouseUpdateMqttReq {

    /**
     * 仓库ID
     */
    @NotNull(message = "仓库ID不能为空")
    private Long id;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 仓库描述
     */
    private String description;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的WarehouseUpdateMqttReq对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static WarehouseUpdateMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, WarehouseUpdateMqttReq.class);
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
