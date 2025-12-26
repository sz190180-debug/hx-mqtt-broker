package com.hx.mqtt.handler.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.dto.GetTaskChainsDto;
import com.hx.mqtt.domain.rep.api.TaskChain;
import com.hx.mqtt.domain.req.mqtt.GetTaskChainsReq;
import com.hx.mqtt.event.TaskChainEndEvent;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.HxUserTaskChainTemplateService;
import com.hx.mqtt.service.RcsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.hx.mqtt.common.enums.TaskChainStatusEnum.EXECUTING;
import static com.hx.mqtt.common.enums.TaskChainStatusEnum.NOT_EXECUTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetTaskChainsHandler implements MqttTopicHandler {

    private final static ConcurrentHashMap<Long, Integer> CACHE = new ConcurrentHashMap<>();

    private final RcsApiService rcsApiService;
    private final HxUserTaskChainTemplateService hxUserTaskChainTemplateService;
    private final ApplicationContext applicationContext;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.GET_TASK_CHAINS;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        GetTaskChainsReq req = GetTaskChainsReq.fromJson(payload);

        List<Long> taskChainIds = req.getTaskChainId();
        if (CollectionUtil.isEmpty(taskChainIds)) {
            return MqttResp.success(context.getReqId());
        }
        List<TaskChain> list =
                JSONArray.parseArray(JSONObject.toJSONString(rcsApiService.getTaskChainByIds(taskChainIds.toArray
                        (new Long[0]))), TaskChain.class);
        if (CollectionUtil.isEmpty(list)) {
            return MqttResp.success(context.getReqId());
        }

        List<GetTaskChainsDto> result = new ArrayList<>();
        Set<Long> idSet = new HashSet<>();
        list.forEach(taskChain -> {
            GetTaskChainsDto dto = new GetTaskChainsDto();
            TaskChain.TaskChainPo taskChainPo = taskChain.getTaskChainPo();
            dto.setStatus(taskChainPo.getStatus());
            dto.setTaskChainId(taskChainPo.getId());
            result.add(dto);
            Integer status = dto.getStatus();
            if (taskChainPo.getAmrId() != null) {
                GlobalCache.TASK_ID_AMR_MAP.put(taskChainPo.getId(), taskChainPo.getAmrId());
            }
            if (NOT_EXECUTED.getCode().equals(status) ||
                    EXECUTING.getCode().equals(status)) {
                return;
            }
            GlobalCache.removeByStatus(status, taskChainPo.getId());
            applicationContext.publishEvent(TaskChainEndEvent.builder()
                    .source(this)
                    .status(7)
                    .taskChainId(dto.getTaskChainId())
                    .amrId(taskChain.getTaskChainPo().getAmrId())
                    .isAutoDispatch(false)
                    .build());
            idSet.add(dto.getTaskChainId());
        });
        return MqttResp.success(context.getReqId(), result);
    }
}
