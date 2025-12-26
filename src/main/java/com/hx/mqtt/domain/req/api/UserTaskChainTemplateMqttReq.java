package com.hx.mqtt.domain.req.api;

import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserTaskChainTemplateMqttReq extends BasePageReq {

    private String groupName;

    /**
     * 排序字段，asc-升序，desc-降序，默认按sortOrder升序
     */
    private String sortOrder = "asc";

    public static UserTaskChainTemplateMqttReq fromJson(String jsonStr) {
        try {
            return JSONObject.parseObject(jsonStr, UserTaskChainTemplateMqttReq.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的JSON格式", e);
        }
    }
}
