package com.hx.mqtt.handler.impl.warehouse;

import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.rep.warehouse.WarehouseColumnRep;
import com.hx.mqtt.domain.req.mqtt.warehouse.WarehouseColumnListMqttReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.WarehouseColumnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 获取仓库的库位列MQTT处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseColumnListHandler implements MqttTopicHandler {

    private final WarehouseColumnService warehouseColumnService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.WAREHOUSE_COLUMN_LIST;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        try {
            WarehouseColumnListMqttReq mqttReq = WarehouseColumnListMqttReq.fromJson(payload);
            log.info("获取仓库的库位列MQTT请求: {}", payload);

            // 调用服务层方法
            List<WarehouseColumnRep> result = warehouseColumnService.getColumnsByWarehouseId(mqttReq.getWarehouseId());

            return MqttResp.success(context.getReqId(), result);
        } catch (Exception e) {
            log.error("获取仓库的库位列失败: {}", e.getMessage(), e);
            return MqttResp.fail(context.getReqId(), "获取仓库的库位列失败: " + e.getMessage());
        }
    }
}
