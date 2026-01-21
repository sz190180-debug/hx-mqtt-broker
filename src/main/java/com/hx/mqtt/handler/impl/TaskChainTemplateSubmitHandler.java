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
import com.hx.mqtt.domain.entity.WarehouseColumn;
import com.hx.mqtt.domain.entity.WarehouseColumnVertexes;
import com.hx.mqtt.domain.rep.mqtt.TaskAddRep;
import com.hx.mqtt.domain.req.mqtt.TaskChainTemplateSubmitReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hx.mqtt.common.GlobalCache.TASK_ID_AMR_MAP;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskChainTemplateSubmitHandler implements MqttTopicHandler {

    private final TaskChainTemplateService taskChainTemplateService;
    private final HxUserTaskChainTemplateService hxUserTaskChainTemplateService;
    private final RcsApiService rcsApiService;
    private final WarehouseColumnService warehouseColumnService;
    private final WarehouseColumnVertexesService warehouseColumnVertexesService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.TASK_TEMPLATE_SUBMIT;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        TaskChainTemplateSubmitReq req = TaskChainTemplateSubmitReq.fromJson(payload);
        log.info("task template submit payload: {}", payload);

        // 1. 基础校验：先查模板是否存在
        LambdaQueryWrapper<TaskChainTemplate> lq = Wrappers.lambdaQuery();
        lq.eq(TaskChainTemplate::getAlias, req.getAlias());
        TaskChainTemplate taskChainTemplate = taskChainTemplateService.getOne(lq);
        if (taskChainTemplate == null) {
            return MqttResp.fail(context.getReqId(), "未能找到任务链模板");
        }

        // 2. 提前查询用户模板关联信息（为了拿到 warehouseId）
        HxUserTaskChainTemplate template =
                hxUserTaskChainTemplateService.selectByClientIdAndChainId(context.getClientId(),
                        taskChainTemplate.getId());
        if (template == null) {
            return MqttResp.fail(context.getReqId(), MqttRespEnum.EXCEPTION.getMsg());
        }

        // 3. 【加锁区域】 synchronized
        // 确保同一时刻只有一个线程能进行“检查+提交”的操作
        synchronized (this) {

            // 仓库列数据
            List<WarehouseColumn> warehouseColumnList = warehouseColumnService.lambdaQuery()
                    .eq(WarehouseColumn::getWarehouseId, template.getWarehouseId())
                    .list();

            if (!warehouseColumnList.isEmpty()) {
                Set<Long> columnIds = warehouseColumnList.stream().map(WarehouseColumn::getColumnId).collect(Collectors.toSet());
                List<WarehouseColumnVertexes> warehouseColumnVertexesList = warehouseColumnVertexesService.lambdaQuery()
                        .in(WarehouseColumnVertexes::getColumnId, columnIds)
                        .list();

                if (warehouseColumnVertexesList.size() == 1) {
                    // 4. 【逻辑修正】先检查该仓库下是否有正在运行的任务
                    // 注意：必须在提交RCS之前检查，否则会导致任务已经下发了才报错
                    List<HxUserTaskChainTemplate> list = hxUserTaskChainTemplateService.lambdaQuery()
                            .eq(HxUserTaskChainTemplate::getWarehouseId, template.getWarehouseId())
                            .list();

                    for (HxUserTaskChainTemplate hxUserTaskChainTemplate : list) {
                        // 如果有关联的任务ID，且该任务ID在缓存中（说明正在运行）
                        if (hxUserTaskChainTemplate.getLastTaskChainId() != null) {
                            if (TASK_ID_AMR_MAP.containsKey(hxUserTaskChainTemplate.getLastTaskChainId())) {
                                log.warn("仓库 {} 正忙，任务 {} 正在运行", template.getWarehouseId(), hxUserTaskChainTemplate.getLastTaskChainId());
                                return MqttResp.fail(context.getReqId(), MqttRespEnum.TASK_RUNNING_FAILED.getMsg());
                            }
                        }
                    }
                }
            }

            // 5. 只有检查通过了，才提交给 RCS
            Integer taskIdInt = rcsApiService.taskChainTemplateSubmit(taskChainTemplate.getId());
            if (taskIdInt == null) {
                return MqttResp.fail(context.getReqId(), MqttRespEnum.TASK_TEMPLATE_FAILED.getMsg());
            }
            Long taskId = Long.valueOf(taskIdInt);

            // 6. 更新数据库状态
            template.setLastTaskChainId(taskId);
            hxUserTaskChainTemplateService.updateById(template);
            log.info("更新用户任务链模板最后一条任务链ID成功，用户ID: {}, 模板ID: {}, 任务链ID: {}",
                    template.getHxUserId(), template.getTaskChainTemplateId(), taskId);

            // 7. 更新缓存
            GlobalCache.TASK_ID_TEMPLATE_MAP.put(taskId, taskChainTemplate.getId());
            GlobalCache.TASK_CLIENT_ID_MAP.put(taskId, context.getClientId());
            GlobalCache.TASK_ID_AMR_MAP.put(taskId, 0L);

            return MqttResp.success(context.getReqId(), new TaskAddRep(taskId, taskChainTemplate.getId()));
        }
    }
}
