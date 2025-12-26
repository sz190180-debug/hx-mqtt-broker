package com.hx.mqtt.domain.req.mqtt.warehouse;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 分配用户仓库权限MQTT请求类
 */
@Data
public class UserWarehouseAssignMqttReq {

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 仓库ID列表
     */
    @NotEmpty(message = "仓库ID列表不能为空")
    private List<Long> warehouseIds;

    /**
     * 分配类型：1-追加，2-覆盖
     */
    @NotNull(message = "分配类型不能为空")
    private Integer assignType;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的UserWarehouseAssignMqttReq对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static UserWarehouseAssignMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, UserWarehouseAssignMqttReq.class);
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
