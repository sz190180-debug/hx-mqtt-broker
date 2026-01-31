package com.hx.mqtt.handler.impl.warehouse;

import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.req.warehouse.WarehouseColumnVertexesUpdateReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 更新库位列点位关联（包含重量）MQTT处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseVertexesUpdateHandler implements MqttTopicHandler {

    private final WarehouseColumnVertexesService warehouseColumnVertexesService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.WAREHOUSE_VERTEXES_UPDATE;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        try {
            // 解析请求，包含 weight 字段
            WarehouseColumnVertexesUpdateReq req = JSONObject.parseObject(payload, WarehouseColumnVertexesUpdateReq.class);

            if (req == null || req.getPositionId() == null) {
                return MqttResp.fail(context.getReqId(), "参数错误");
            }

            // 调用 Service 更新 (Service层已包含处理重量的逻辑)
            Boolean result = warehouseColumnVertexesService.updateWarehouseColumnVertexes(req);

            if (result) {
                return MqttResp.success(context.getReqId(), true);
            } else {
                return MqttResp.fail(context.getReqId(), "更新失败");
            }
        } catch (Exception e) {
            log.error("更新点位关联失败: {}", e.getMessage(), e);
            return MqttResp.fail(context.getReqId(), "系统异常: " + e.getMessage());
        }
    }
}