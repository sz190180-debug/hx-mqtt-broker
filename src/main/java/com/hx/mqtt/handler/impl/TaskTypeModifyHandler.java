package com.hx.mqtt.handler.impl;

import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.entity.HxMapVertexes;
import com.hx.mqtt.domain.req.mqtt.TaskTypeModifyReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.HxMapVertexesService;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskTypeModifyHandler implements MqttTopicHandler {
    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.TASK_TYPE_MODIFY;
    }

    private final HxMapVertexesService hxMapVertexesService;
    private final WarehouseColumnVertexesService warehouseColumnVertexesService;

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        TaskTypeModifyReq request = TaskTypeModifyReq.fromJson(payload);

        if (request.getEndPointCode() == null || request.getEndPointCode().isEmpty()) {
            log.warn("【TASK_TYPE_MODIFY】endPointCode为空");
            return MqttResp.success(context.getReqId());
        }

        // 遍历所有的别名和对应的值
        request.getEndPointCode().forEach((alias, value) -> {
            try {
                updateVertexStatusByAlias(alias, value);
            } catch (Exception e) {
                log.error("【TASK_TYPE_MODIFY】更新点位状态失败，别名：{}，值：{}", alias, value, e);
            }
        });

        return MqttResp.success(context.getReqId());
    }

    /**
     * 根据别名更新点位状态
     *
     * @param alias 点位别名
     * @param value 状态值
     */
    private void updateVertexStatusByAlias(String alias, Object value) {
        if (alias == null || alias.trim().isEmpty()) {
            log.warn("【TASK_TYPE_MODIFY】别名为空，跳过处理");
            return;
        }

        // 根据别名查询地图点位
        HxMapVertexes mapVertex = hxMapVertexesService.getByCodeAlias(alias);
        if (mapVertex == null) {
            log.warn("【TASK_TYPE_MODIFY】未找到别名为 {} 的地图点位", alias);
            return;
        }

        // 转换状态值
        Integer status = (Integer) value;
        if (status == null) {
            log.warn("【TASK_TYPE_MODIFY】无效的状态值：{}，别名：{}", null, alias);
            return;
        }
        // 0-可用，1-占用
        int updateStatus;
        if (status == 1) {
            updateStatus = 2;
        } else if (status == 0) {
            updateStatus = 1;
        } else {
            log.error("未知的TASK_TYPE_MODIFY:{},status{}", alias, status);
            return;
        }

        // 更新库位列绑定的点位状态
        boolean updateResult = warehouseColumnVertexesService.updateStatusByMapVertexId(mapVertex.getId(), updateStatus);
        if (updateResult) {
            log.info("【TASK_TYPE_MODIFY】成功更新点位状态，别名：{}，点位ID：{}，新状态：{}",
                    alias, mapVertex.getId(), status);
        } else {
            log.warn("【TASK_TYPE_MODIFY】更新点位状态失败，别名：{}，点位ID：{}，状态：{}",
                    alias, mapVertex.getId(), status);
        }
    }
}
