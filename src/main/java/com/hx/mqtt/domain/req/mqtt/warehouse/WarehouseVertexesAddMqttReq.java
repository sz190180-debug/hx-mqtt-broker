package com.hx.mqtt.domain.req.mqtt.warehouse;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 添加库位列点位关联MQTT请求类
 */
@Data
public class WarehouseVertexesAddMqttReq {

    /**
     * 库位列ID
     */
    @NotNull(message = "库位列ID不能为空")
    private Long columnId;

    /**
     * 地图点位ID
     */
    @NotNull(message = "地图点位ID不能为空")
    private Long hxMapVertexesId;

    /**
     * 点位排序
     */
    private Integer positionOrder;

    /**
     * 状态 (1-可用, 2-占用)
     */
    private Integer status = 1;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的WarehouseVertexesAddMqttReq对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static WarehouseVertexesAddMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, WarehouseVertexesAddMqttReq.class);
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
