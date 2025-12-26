package com.hx.mqtt.handler.impl;

import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.MqttRespEnum;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.rep.mqtt.MsgRep;
import com.hx.mqtt.domain.req.mqtt.TaskResumeReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.RcsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskResumeHandler implements MqttTopicHandler {

    private final RcsApiService rcsApiService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.TASK_RESUME;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        TaskResumeReq req = TaskResumeReq.fromJson(payload);
        String msg = rcsApiService.taskResume(req.getAmrId());
        if (msg == null) {
            return MqttResp.fail(context.getReqId(), MqttRespEnum.EXCEPTION);
        }
        return MqttResp.success(context.getReqId(), new MsgRep(msg));
    }
}
