package com.hx.mqtt.handler.impl.warehouse;

import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.rep.warehouse.WarehouseColumnVertexesRep;
import com.hx.mqtt.domain.req.mqtt.warehouse.WarehouseVertexesListMqttReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 获取库位列的点位关联列表MQTT处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseVertexesListHandler implements MqttTopicHandler {

    private final WarehouseColumnVertexesService warehouseColumnVertexesService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.WAREHOUSE_VERTEXES_LIST;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        try {
            WarehouseVertexesListMqttReq mqttReq = WarehouseVertexesListMqttReq.fromJson(payload);
            log.info("获取库位列的点位关联列表MQTT请求: {}", payload);

            // 调用服务层方法
            List<WarehouseColumnVertexesRep> result = warehouseColumnVertexesService.getVertexesByColumnId(mqttReq.getColumnId());

            return MqttResp.success(context.getReqId(), result);
        } catch (Exception e) {
            log.error("获取库位列的点位关联列表失败: {}", e.getMessage(), e);
            return MqttResp.fail(context.getReqId(), "获取库位列的点位关联列表失败: " + e.getMessage());
        }
    }
}
