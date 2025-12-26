package com.hx.mqtt.handler.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.MqttRespEnum;
import com.hx.mqtt.common.enums.TaskTypeEnum;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.dto.TaskTypeDto;
import com.hx.mqtt.domain.entity.HxMapVertexes;
import com.hx.mqtt.domain.rep.mqtt.TaskAddRep;
import com.hx.mqtt.domain.req.api.TaskChainAddReq;
import com.hx.mqtt.domain.req.mqtt.TaskAddReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.mapper.HxMapVertexesMapper;
import com.hx.mqtt.service.RcsApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAddHandler implements MqttTopicHandler {

    private final HxMapVertexesMapper hxMapVertexesMapper;
    private final RcsApiService rcsApiService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.TASK_ADD;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        TaskAddReq request = TaskAddReq.fromJson(payload);
        log.info("task add {}", payload);
        return doHandle(context, request);
    }

    public MqttResp<?> doHandle(MqttContext context, TaskAddReq request) {
        LambdaQueryWrapper<HxMapVertexes> lq = Wrappers.lambdaQuery();
        lq.in(HxMapVertexes::getCodeAlias, request.getEndPointCode().keySet());
        List<HxMapVertexes> hxMapVertexesList = hxMapVertexesMapper.selectList(lq);
        if (CollUtil.isEmpty(hxMapVertexesList) || hxMapVertexesList.size() != request.getEndPointCode().size()) {
            log.error("task add get code {} unexpected", request.getEndPointCode());
            return MqttResp.fail(context.getReqId(), MqttRespEnum.NONE_VERTEXES_CODE);
        }
        AtomicReference<Long> reference = new AtomicReference<>();
        Map<String, HxMapVertexes> map =
                hxMapVertexesList.stream().peek(v -> reference.set(v.getAreaId())).collect(Collectors.toMap(HxMapVertexes::getCodeAlias, v -> v));
        TaskChainAddReq taskChainAddReq = new TaskChainAddReq();
        TaskChainAddReq.TaskChain taskChain = new TaskChainAddReq.TaskChain();
        BeanUtils.copyProperties(request, taskChain);
        if (Objects.equals(request.getAmrId(), 0L)) {
            taskChain.setAmrId(null);
        }
        taskChain.setIsReturn(1);
        taskChain.setAreaId(reference.get());
        taskChainAddReq.setTaskChain(taskChain);

        taskChainAddReq.setTasks(request.getEndPointCode().entrySet().stream().map(entry -> {
            HxMapVertexes hxMapVertexes = map.get(entry.getKey());
            TaskChainAddReq.TaskPo po = new TaskChainAddReq.TaskPo();
            po.setMapId(hxMapVertexes.getMapId());
            po.setEndPointCode(hxMapVertexes.getCode());
            po.setTaskType(TaskTypeEnum.getByCode((Integer) entry.getValue()).name());
            return po;
        }).collect(Collectors.toList()));

        Integer taskIdInt = rcsApiService.taskAdd(taskChainAddReq);
        if (taskIdInt == null) {
            return MqttResp.fail(context.getReqId(), MqttRespEnum.EXCEPTION);
        }
        Long taskId = Long.valueOf(taskIdInt);

        GlobalCache.TASK_CLIENT_ID_MAP.put(taskId, context.getClientId());
        GlobalCache.TASK_ID_AMR_MAP.put(taskId, 0L);

        return MqttResp.success(context.getReqId(), new TaskAddRep(taskId, null));
    }
}
