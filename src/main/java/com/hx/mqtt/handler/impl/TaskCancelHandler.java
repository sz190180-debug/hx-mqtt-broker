package com.hx.mqtt.handler.impl;

import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.MqttRespEnum;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.req.mqtt.TaskCancelReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.RcsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskCancelHandler implements MqttTopicHandler {

    private final RcsApiService rcsApiService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.TASK_CANCEL;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        TaskCancelReq req = TaskCancelReq.fromJson(payload);
        Object o = rcsApiService.taskCancel(req.getTaskChainId());
        if (o == null) {
            return MqttResp.fail(context.getReqId(), MqttRespEnum.EXCEPTION);
        }
        return MqttResp.success(context.getReqId());
    }
}
