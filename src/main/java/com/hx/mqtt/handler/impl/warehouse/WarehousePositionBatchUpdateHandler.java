package com.hx.mqtt.handler.impl.warehouse;

import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.rep.warehouse.BatchUpdatePositionStatusRep;
import com.hx.mqtt.domain.req.mqtt.warehouse.WarehousePositionBatchUpdateMqttReq;
import com.hx.mqtt.domain.req.warehouse.BatchUpdatePositionStatusReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 批量更新点位状态MQTT处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarehousePositionBatchUpdateHandler implements MqttTopicHandler {

    private final WarehouseColumnVertexesService warehouseColumnVertexesService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.WAREHOUSE_POSITION_BATCH_UPDATE;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        try {
            WarehousePositionBatchUpdateMqttReq mqttReq = WarehousePositionBatchUpdateMqttReq.fromJson(payload);
            log.info("批量更新点位状态MQTT请求: {}", payload);

            // 转换为服务层请求对象
            BatchUpdatePositionStatusReq serviceReq = new BatchUpdatePositionStatusReq();
            BeanUtils.copyProperties(mqttReq, serviceReq);

            // 调用服务层方法
            BatchUpdatePositionStatusRep result = warehouseColumnVertexesService.batchUpdatePositionStatus(serviceReq);

            return MqttResp.success(context.getReqId(), result);
        } catch (Exception e) {
            log.error("批量更新点位状态失败: {}", e.getMessage(), e);
            return MqttResp.fail(context.getReqId(), "批量更新点位状态失败: " + e.getMessage());
        }
    }
}
