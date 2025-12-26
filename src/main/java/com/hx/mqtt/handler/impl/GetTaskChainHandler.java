package com.hx.mqtt.handler.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.MqttRespEnum;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.dto.GetTaskChainDto;
import com.hx.mqtt.domain.rep.api.TaskChain;
import com.hx.mqtt.domain.req.mqtt.GetTaskChainReq;
import com.hx.mqtt.event.TaskChainEndEvent;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.RcsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetTaskChainHandler implements MqttTopicHandler {

    private final static ConcurrentHashMap<Long, Integer> CACHE = new ConcurrentHashMap<>();

    @Autowired
    private RcsApiService rcsApiService;
    private ApplicationContext applicationContext;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.GET_TASK_CHAIN;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        GetTaskChainReq req = GetTaskChainReq.fromJson(payload);

        Long taskChainId = req.getTaskChainId();
        List<TaskChain> list =
                JSONArray.parseArray(JSONObject.toJSONString(rcsApiService.getTaskChainByIds(taskChainId)),
                        TaskChain.class);
        if (CollectionUtil.isEmpty(list)) {
            return MqttResp.success(context.getReqId());
        }
        Integer currentStatus = CACHE.get(taskChainId);
        TaskChain.TaskChainPo taskChainPo = list.get(0).getTaskChainPo();
        Integer status = taskChainPo.getStatus();
        if (status == null) {
            return MqttResp.fail(context.getReqId(), MqttRespEnum.NONE_EXIST_USER);
        }
        if (status == 3 || status == 5 || status == 6 || status == 7) {
            applicationContext.publishEvent(TaskChainEndEvent.builder()
                    .source(this)
                    .status(status)
                    .taskChainId(req.getTaskChainId())
                    .amrId(taskChainPo.getAmrId())
                    .isAutoDispatch(false)
                    .build());
        }
        if (currentStatus != null && Objects.equals(currentStatus, status)) {
            context.setSendRep(false);
        }
        CACHE.put(taskChainId, status);
        GetTaskChainDto dto = new GetTaskChainDto();
        dto.setStatus(status);
        return MqttResp.success(context.getReqId(), dto);
    }
}
