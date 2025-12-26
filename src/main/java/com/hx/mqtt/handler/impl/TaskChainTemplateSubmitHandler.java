package com.hx.mqtt.handler.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.MqttRespEnum;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.entity.HxUserTaskChainTemplate;
import com.hx.mqtt.domain.entity.TaskChainTemplate;
import com.hx.mqtt.domain.rep.mqtt.TaskAddRep;
import com.hx.mqtt.domain.req.mqtt.TaskChainTemplateSubmitReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.HxUserTaskChainTemplateService;
import com.hx.mqtt.service.RcsApiService;
import com.hx.mqtt.service.TaskChainTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskChainTemplateSubmitHandler implements MqttTopicHandler {

    private final TaskChainTemplateService taskChainTemplateService;
    private final HxUserTaskChainTemplateService hxUserTaskChainTemplateService;
    private final RcsApiService rcsApiService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.TASK_TEMPLATE_SUBMIT;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        TaskChainTemplateSubmitReq req = TaskChainTemplateSubmitReq.fromJson(payload);
        LambdaQueryWrapper<TaskChainTemplate> lq = Wrappers.lambdaQuery();
        lq.eq(TaskChainTemplate::getAlias, req.getAlias());
        TaskChainTemplate taskChainTemplate = taskChainTemplateService.getOne(lq);
        if (taskChainTemplate == null) {
            return MqttResp.fail(context.getReqId(), "未能找到任务链模板");
        }

        Integer taskIdInt = rcsApiService.taskChainTemplateSubmit(taskChainTemplate.getId());
        if (taskIdInt == null) {
            return MqttResp.fail(context.getReqId(), MqttRespEnum.TASK_TEMPLATE_FAILED.getMsg());
        }
        Long taskId = Long.valueOf(taskIdInt);

        HxUserTaskChainTemplate template =
                hxUserTaskChainTemplateService.selectByClientIdAndChainId(context.getClientId(),
                        taskChainTemplate.getId());
        if (template != null) {
            template.setLastTaskChainId(taskId);
            hxUserTaskChainTemplateService.updateById(template);
            log.info("更新用户任务链模板最后一条任务链ID成功，用户ID: {}, 模板ID: {}, 任务链ID: {}",
                    template.getHxUserId(), template.getTaskChainTemplateId(), taskId);
        }

        GlobalCache.TASK_ID_TEMPLATE_MAP.put(taskId, taskChainTemplate.getId());
        GlobalCache.TASK_CLIENT_ID_MAP.put(taskId, context.getClientId());
        GlobalCache.TASK_ID_AMR_MAP.put(taskId, 0L);
        return MqttResp.success(context.getReqId(), new TaskAddRep(taskId, taskChainTemplate.getId()));
    }
}
