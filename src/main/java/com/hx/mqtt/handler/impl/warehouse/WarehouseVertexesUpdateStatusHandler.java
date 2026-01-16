package com.hx.mqtt.handler.impl.warehouse;

import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.entity.WarehouseColumn;
import com.hx.mqtt.domain.entity.WarehouseColumnVertexes;
import com.hx.mqtt.domain.rep.warehouse.BatchUpdatePositionStatusRep;
import com.hx.mqtt.domain.rep.warehouse.WarehouseColumnVertexesRep;
import com.hx.mqtt.domain.req.mqtt.warehouse.WarehouseVertexesListMqttReq;
import com.hx.mqtt.domain.req.warehouse.BatchUpdatePositionStatusReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.WarehouseColumnService;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 获取库位列的点位关联列表MQTT处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseVertexesUpdateStatusHandler implements MqttTopicHandler {

    private final WarehouseColumnVertexesService warehouseColumnVertexesService;

    private final WarehouseColumnService warehouseColumnService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.WAREHOUSE_VERTEXES_UPDATE_STATUS;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        try {
            HashMap<String, Integer> hashMap = JSONObject.parseObject(payload, HashMap.class);

            hashMap.forEach((k, v) -> {
                if (k.equals("reqId")) {
                    return;
                }

                WarehouseColumn warehouseColumn = warehouseColumnService.lambdaQuery()
                        .eq(WarehouseColumn::getColumnName, k)
                        .last("limit 1")
                        .one();

                if (warehouseColumn == null) {
                    log.warn("没有找到任何点位，请求的库名: {}", k);
                    return;
                }

                WarehouseColumnVertexes warehouseColumnVertexes = warehouseColumnVertexesService.lambdaQuery()
                        .eq(WarehouseColumnVertexes::getColumnId, warehouseColumn.getColumnId())
                        .last("limit 1")
                        .one();

                if (warehouseColumnVertexes == null) {
                    log.warn("没有找到任何点位，请求的ID: {}", warehouseColumn.getColumnId());
                    return;
                }

                warehouseColumnVertexes.setStatus(v);

                boolean update = warehouseColumnVertexesService.updateById(warehouseColumnVertexes);

                log.info("{}更新点位状态：{}", k, update);

            });

            return MqttResp.success(context.getReqId(), null);
        } catch (Exception e) {
            log.error("更新库位列的点位关联列表失败: {}", e.getMessage(), e);
            return MqttResp.fail(context.getReqId(), "更新库位列的点位关联列表失败: " + e.getMessage());
        }
    }

}
