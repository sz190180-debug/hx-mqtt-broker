package com.hx.mqtt.domain.req.mqtt.warehouse;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新库位列MQTT请求类
 */
@Data
public class WarehouseColumnUpdateMqttReq {

    /**
     * 库位列ID
     */
    @NotNull(message = "库位列ID不能为空")
    private Long id;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 库位列名称
     */
    private String columnName;

    /**
     * 库位列排序
     */
    private Integer columnOrder;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的WarehouseColumnUpdateMqttReq对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static WarehouseColumnUpdateMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, WarehouseColumnUpdateMqttReq.class);
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
