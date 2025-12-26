package com.hx.mqtt.domain.req.mqtt.warehouse;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量更新点位状态MQTT请求类
 */
@Data
public class WarehousePositionBatchUpdateMqttReq {

    /**
     * 点位ID列表
     */
    @NotEmpty(message = "点位ID列表不能为空")
    private List<Long> positionIds;

    /**
     * 状态 (1-可用, 2-占用)
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的WarehousePositionBatchUpdateMqttReq对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static WarehousePositionBatchUpdateMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, WarehousePositionBatchUpdateMqttReq.class);
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
