package com.hx.mqtt.domain.req.mqtt;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class TaskTypeModifyReq {

    /**
     * 目标点编码
     * 示例值：A1
     */
    private JSONObject endPointCode;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的MqttAccountRequest对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static TaskTypeModifyReq fromJson(String jsonStr) {
        try {
            JSONObject parse = JSONObject.parseObject(jsonStr);
            TaskTypeModifyReq taskTypeModifyReq = new TaskTypeModifyReq();
            parse.remove("reqId");
            taskTypeModifyReq.setEndPointCode(parse);
            return taskTypeModifyReq;
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
