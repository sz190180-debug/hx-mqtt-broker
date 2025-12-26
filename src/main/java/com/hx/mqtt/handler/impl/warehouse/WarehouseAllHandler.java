package com.hx.mqtt.handler.impl.warehouse;

import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.rep.warehouse.WarehouseRep;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 获取所有仓库MQTT处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseAllHandler implements MqttTopicHandler {

    private final WarehouseService warehouseService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.WAREHOUSE_ALL;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        try {
            log.info("获取所有仓库MQTT请求: {}", payload);

            // 调用带权限过滤的服务层方法
            List<WarehouseRep> result = warehouseService.getAllWarehousesByClientId(context.getClientId());

            return MqttResp.success(context.getReqId(), result);
        } catch (Exception e) {
            log.error("获取所有仓库失败: {}", e.getMessage(), e);
            return MqttResp.fail(context.getReqId(), "获取所有仓库失败: " + e.getMessage());
        }
    }
}
