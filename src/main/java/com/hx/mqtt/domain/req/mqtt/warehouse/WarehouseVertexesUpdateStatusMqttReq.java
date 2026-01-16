 package com.hx.mqtt.domain.req.mqtt.warehouse;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取库位列的点位关联列表MQTT请求类
 */
@Data
public class WarehouseVertexesUpdateStatusMqttReq {

    /**
     * 库位列ID
     */
    @NotNull(message = "库位列ID不能为空")
    private Long columnId;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的WarehouseVertexesListMqttReq对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static WarehouseVertexesUpdateStatusMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, WarehouseVertexesUpdateStatusMqttReq.class);
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
