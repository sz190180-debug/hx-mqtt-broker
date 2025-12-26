package com.hx.mqtt.domain.req.mqtt;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class GetTaskChainReq {
    /**
     * 任务链id
     */
    private Long taskChainId;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的MqttAccountRequest对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static GetTaskChainReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, GetTaskChainReq.class);
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
