package com.hx.mqtt.domain.req.mqtt;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class TaskChainTemplateSubmitReq {

    /**
     * 模板别名
     */
    private String alias;

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @return 解析后的MqttAccountRequest对象
     * @throws IllegalArgumentException 当JSON格式不合法时抛出
     */
    public static TaskChainTemplateSubmitReq fromJson(String jsonStr) {
        try {
            JSONObject parse = JSONObject.parseObject(jsonStr);
            TaskChainTemplateSubmitReq req = new TaskChainTemplateSubmitReq();
            parse.remove("reqId");
            req.setAlias(CollectionUtil.getFirst(parse.keySet()));
            return req;
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
