package com.hx.mqtt.event;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.enums.TaskChainTypeEnum;
import com.hx.mqtt.domain.dto.MachineControlState;
import com.hx.mqtt.domain.entity.HxMapVertexes;
import com.hx.mqtt.domain.entity.HxUserTaskChainTemplate;
import com.hx.mqtt.domain.entity.TaskChainTemplate;
import com.hx.mqtt.handler.impl.TaskAddHandler;
import com.hx.mqtt.service.HxUserTaskChainTemplateService;
import com.hx.mqtt.service.TaskChainTemplateService;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.hx.mqtt.common.GlobalCache.CONTROL_STATE_MAP;
import static com.hx.mqtt.common.GlobalCache.MACHINE_TASK_MAP;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskChainEndEventHandler implements ApplicationListener<TaskChainEndEvent> {

    private final HxUserTaskChainTemplateService hxUserTaskChainTemplateService;
    private final WarehouseColumnVertexesService warehouseColumnVertexesService;
    private final TaskChainTemplateService taskChainTemplateService;
    private final TaskAddHandler taskAddHandler;

    @Async
    @Override
    public void onApplicationEvent(TaskChainEndEvent event) {
        if (MACHINE_TASK_MAP.containsKey(event.getTaskChainId())) {
            Long remove = MACHINE_TASK_MAP.remove(event.getTaskChainId());
            log.info("接收到任务链{}完成信号,移除机械手控制", remove);
            if (remove != null) {
                MachineControlState machineControlState = CONTROL_STATE_MAP.get(remove);
                machineControlState.setCurrentAmrId(event.getAmrId());
            }
            return;
        }
        Integer status = event.getStatus();
        Long taskChainId = event.getTaskChainId();
        GlobalCache.removeByStatus(status, taskChainId);
        LambdaQueryWrapper<HxUserTaskChainTemplate> lq = Wrappers.lambdaQuery();
        lq.eq(HxUserTaskChainTemplate::getLastTaskChainId, taskChainId);
        lq.orderByDesc(HxUserTaskChainTemplate::getId);
        lq.last("limit 1");
        HxUserTaskChainTemplate hxUserTaskChainTemplate = hxUserTaskChainTemplateService.getOne(lq);
        if (!GlobalCache.TASK_ID_TEMPLATE_MAP.containsKey(taskChainId)) {
            LambdaUpdateWrapper<HxUserTaskChainTemplate> upLq = Wrappers.lambdaUpdate();
            upLq.eq(HxUserTaskChainTemplate::getLastTaskChainId, taskChainId)
                    .set(HxUserTaskChainTemplate::getLastTaskChainId, null);
            hxUserTaskChainTemplateService.update(upLq);
            log.info("更新taskChainId={}的lastTaskChainId为null", taskChainId);
        }
        if (event.getIsAutoDispatch() == null || !event.getIsAutoDispatch()) {
            log.info("未设置自动下发任务不进行下发");
            return;
        }
        Long templateId = GlobalCache.TASK_ID_TEMPLATE_MAP.remove(taskChainId);

        // 处理仓库位置状态更新
        TaskChainTemplate template = taskChainTemplateService.getById(templateId);
        if (template == null) {
            log.error("未知的templateId:{}", templateId);
            return;
        }
        updateWarehousePositionStatus(event, template, hxUserTaskChainTemplate);
    }

    /**
     * 根据
     * 上料任务（taskType=0）：将位置状态改为可用（status=1）
     * 下料任务（taskType=1）：将位置状态改为占用（status=2）
     */
    private void updateWarehousePositionStatus(TaskChainEndEvent event, TaskChainTemplate template,
                                               HxUserTaskChainTemplate hxUserTaskChainTemplate) {
        try {
            // 只处理已完成的任务（状态7）
            if (event.getStatus() == null || event.getStatus() != 7) {
                log.info("任务状态为{}，不是完成状态，跳过仓库位置状态更新", event.getStatus());
                return;
            }

            if (TaskChainTypeEnum.INBOUND.getCode().equals(template.getChainType())) {
                // 查找最远可用位置并自动下发下料任务
                if (hxUserTaskChainTemplate != null && hxUserTaskChainTemplate.getWarehouseId() != null && event.getAmrId() != null) {
                    autoDispatchUnloadingTask(hxUserTaskChainTemplate, event.getAmrId());
                } else {
                    if (event.getAmrId() == null) {
                        log.info("AMR ID不存在，无法自动下发下料任务");
                    }
                    if (hxUserTaskChainTemplate == null) {
                        log.info("用户任务链模板不存在，无法自动下发下料任务");
                    } else if (hxUserTaskChainTemplate.getWarehouseId() == null) {
                        log.info("用户任务链模板未指定仓库，无法自动下发下料任务");
                    }
                }
            } else {
                log.info("任务链类型为{}，不是入库类型，跳过仓库位置状态更新", template.getChainType());
            }
        } catch (Exception e) {
            log.error("updateWarehousePositionStatus :", e);
        }
    }

    /**
     * 自动下发下料任务
     * 根据用户权限查找最远可用位置并下发下料任务
     */
    private void autoDispatchUnloadingTask(HxUserTaskChainTemplate template, Long amrId) {
        try {
            if (template == null || template.getWarehouseId() == null) {
                log.warn("模板信息不完整，无法自动下发下料任务");
                return;
            }

            // 查找最远可用位置
            HxMapVertexes targetPosition = warehouseColumnVertexesService.findFarthestAvailablePosition(
                    template.getWarehouseId(), template.getColumnId());

            if (targetPosition == null) {
                log.warn("无法找到可用点位，无法自动下发下料任务。仓库ID: {}, 库位列ID: {}",
                        template.getWarehouseId(), template.getColumnId());
                return;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(targetPosition.getCodeAlias(), 8);
            jsonObject.put("amrId", amrId);

            // 创建虚拟的MQTT上下文
            MqttContext context = new MqttContext("AUTO_UNLOAD", -1L, false);

            // 下发下料任务
            var result = taskAddHandler.handle(context, jsonObject.toJSONString());

            if (result != null && result.getCode() == 10000) {
                log.info("成功自动下发下料任务到点位: {}, AMR: {}", targetPosition.getCodeAlias(), amrId);
            } else {
                log.warn("自动下发下料任务失败，点位: {}, AMR: {}, 结果: {}",
                        targetPosition.getCodeAlias(), amrId, result);
            }

        } catch (Exception e) {
            log.error("自动下发下料任务时发生异常，模板: {}, AMR: {}",
                    JSONObject.toJSONString(template), amrId, e);
        }
    }
}
