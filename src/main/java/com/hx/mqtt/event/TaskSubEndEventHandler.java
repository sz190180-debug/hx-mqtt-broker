package com.hx.mqtt.event;

import com.hx.mqtt.common.enums.TaskTypeEnum;
import com.hx.mqtt.domain.entity.HxMapVertexes;
import com.hx.mqtt.service.HxMapVertexesService;
import com.hx.mqtt.service.WarehouseColumnVertexesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSubEndEventHandler implements ApplicationListener<TaskSubEndEvent> {

    private final HxMapVertexesService hxMapVertexesService;
    private final WarehouseColumnVertexesService warehouseColumnVertexesService;

    @Async
    @Override
    public void onApplicationEvent(TaskSubEndEvent event) {
        Integer taskType = TaskTypeEnum.getTaskType(TaskTypeEnum.valueOf(event.getTaskType()));
        if (taskType == null) {
            log.info("任务类型{}不存在,不进行状态更细", event.getTaskType());
            return;
        }

        HxMapVertexes hxMapVertexes = hxMapVertexesService.getByCode(event.getEndPointCode());
        if (hxMapVertexes == null) {
            log.info("点位{}数据存在,不进行状态更细", event.getEndPointCode());
            return;
        }

        if (taskType == 0) {
            // 上料任务完成，位置变为可用
            warehouseColumnVertexesService.updateStatusByMapVertexId(hxMapVertexes.getId(), 1);
        } else if (taskType == 1) {
            // 下料任务完成，位置变为占用
            warehouseColumnVertexesService.updateStatusByMapVertexId(hxMapVertexes.getId(), 2);
        }
    }
}
